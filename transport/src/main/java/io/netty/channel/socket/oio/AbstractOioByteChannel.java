/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.socket.oio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.ChannelInputShutdownEvent;

import java.io.IOException;

/**
 * Abstract base class for OIO which reads and writes bytes from/to a Socket
 */
public abstract class AbstractOioByteChannel extends AbstractOioChannel {

    private volatile boolean inputShutdown;

    /**
     * @see AbstractOioByteChannel#AbstractOioByteChannel(Channel, Integer)
     */
    protected AbstractOioByteChannel(Channel parent, Integer id) {
        super(parent, id);
    }

    boolean isInputShutdown() {
        return inputShutdown;
    }

    @Override
    protected void doRead() {
        if (inputShutdown) {
            try {
                Thread.sleep(SO_TIMEOUT);
            } catch (InterruptedException e) {
                // ignore
            }
            return;
        }

        final ChannelPipeline pipeline = pipeline();
        final ByteBuf byteBuf = pipeline.inboundByteBuffer();
        boolean closed = false;
        boolean read = false;
        boolean firedInboundBufferSuspeneded = false;
        try {
            expandReadBuffer(byteBuf);
            loop: for (;;) {
                int localReadAmount = doReadBytes(byteBuf);
                if (localReadAmount > 0) {
                    read = true;
                } else if (localReadAmount < 0) {
                    closed = true;
                    break;
                }

                final int available = available();
                if (available <= 0) {
                    break;
                }

                switch (expandReadBuffer(byteBuf)) {
                    case 0:
                        // Read all - stop reading.
                        break loop;
                    case 1:
                        // Keep reading until everything is read.
                        break;
                    case 2:
                        // Let the inbound handler drain the buffer and continue reading.
                        if (read) {
                            read = false;
                            pipeline.fireInboundBufferUpdated();
                            if (!byteBuf.writable()) {
                                throw new IllegalStateException(
                                        "an inbound handler whose buffer is full must consume at " +
                                                "least one byte.");
                            }
                        }
                }
            }
        } catch (Throwable t) {
            if (read) {
                read = false;
                pipeline.fireInboundBufferUpdated();
            }
            firedInboundBufferSuspeneded = true;
            pipeline.fireInboundBufferSuspended();
            pipeline.fireExceptionCaught(t);
            if (t instanceof IOException) {
                unsafe().close(unsafe().voidFuture());
            }
        } finally {
            if (read) {
                pipeline.fireInboundBufferUpdated();
            }
            if (!firedInboundBufferSuspeneded) {
                pipeline.fireInboundBufferSuspended();
            }
            if (closed) {
                inputShutdown = true;
                if (isOpen()) {
                    if (Boolean.TRUE.equals(config().getOption(ChannelOption.ALLOW_HALF_CLOSURE))) {
                        pipeline.fireUserEventTriggered(ChannelInputShutdownEvent.INSTANCE);
                    } else {
                        unsafe().close(unsafe().voidFuture());
                    }
                }
            }
        }
    }

    @Override
    protected void doFlushByteBuffer(ByteBuf buf) throws Exception {
        while (buf.readable()) {
            doWriteBytes(buf);
        }
        buf.clear();
    }

    /**
     * Return the number of bytes ready to read from the underlying Socket.
     */
    protected abstract int available();

    /**
     * Read bytes from the underlying Socket.
     *
     * @param buf           the {@link ByteBuf} into which the read bytes will be written
     * @return amount       the number of bytes read. This may return a negative amount if the underlying
     *                      Socket was closed
     * @throws Exception    is thrown if an error accoured
     */
    protected abstract int doReadBytes(ByteBuf buf) throws Exception;

    /**
     * Write the data which is hold by the {@link ByteBuf} to the underlying Socket.
     *
     * @param buf           the {@link ByteBuf} which holds the data to transfer
     * @throws Exception    is thrown if an error accoured
     */
    protected abstract void doWriteBytes(ByteBuf buf) throws Exception;
}
