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
package io.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.UniqueName;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link ChannelOption} allows to configure a {@link ChannelConfig} in a type-safe
 * way. Which {@link ChannelOption} is supported depends on the actual implementation
 * of {@link ChannelConfig} and may depend on the nature of the transport it belongs
 * to.
 *
 * @param <T>   the type of the value which is valid for the {@link ChannelOption}
 */
public class ChannelOption<T> extends UniqueName {

    private static final ConcurrentMap<String, Boolean> names = new ConcurrentHashMap<String, Boolean>();

    public static final ChannelOption<ByteBufAllocator> ALLOCATOR = new ChannelOption<ByteBufAllocator>("ALLOCATOR");
    public static final ChannelOption<Integer> CONNECT_TIMEOUT_MILLIS =
            new ChannelOption<Integer>("CONNECT_TIMEOUT_MILLIS");
    public static final ChannelOption<Integer> WRITE_SPIN_COUNT =
            new ChannelOption<Integer>("WRITE_SPIN_COUNT");
    public static final ChannelOption<Boolean> ALLOW_HALF_CLOSURE =
            new ChannelOption<Boolean>("ALLOW_HALF_CLOSURE");
    public static final ChannelOption<Boolean> AUTO_READ =
            new ChannelOption<Boolean>("AUTO_READ");

    public static final ChannelOption<Boolean> SO_BROADCAST =
            new ChannelOption<Boolean>("SO_BROADCAST");
    public static final ChannelOption<Boolean> SO_KEEPALIVE =
            new ChannelOption<Boolean>("SO_KEEPALIVE");
    public static final ChannelOption<Integer> SO_SNDBUF =
            new ChannelOption<Integer>("SO_SNDBUF");
    public static final ChannelOption<Integer> SO_RCVBUF =
            new ChannelOption<Integer>("SO_RCVBUF");
    public static final ChannelOption<Boolean> SO_REUSEADDR =
            new ChannelOption<Boolean>("SO_REUSEADDR");
    public static final ChannelOption<Integer> SO_LINGER =
            new ChannelOption<Integer>("SO_LINGER");
    public static final ChannelOption<Integer> SO_BACKLOG =
            new ChannelOption<Integer>("SO_BACKLOG");

    public static final ChannelOption<Integer> IP_TOS =
            new ChannelOption<Integer>("IP_TOS");
    public static final ChannelOption<InetAddress> IP_MULTICAST_ADDR =
            new ChannelOption<InetAddress>("IP_MULTICAST_ADDR");
    public static final ChannelOption<NetworkInterface> IP_MULTICAST_IF =
            new ChannelOption<NetworkInterface>("IP_MULTICAST_IF");
    public static final ChannelOption<Integer> IP_MULTICAST_TTL =
            new ChannelOption<Integer>("IP_MULTICAST_TTL");
    public static final ChannelOption<Boolean> IP_MULTICAST_LOOP_DISABLED =
            new ChannelOption<Boolean>("IP_MULTICAST_LOOP_DISABLED");

    public static final ChannelOption<Integer> UDP_RECEIVE_PACKET_SIZE =
            new ChannelOption<Integer>("UDP_RECEIVE_PACKET_SIZE");

    public static final ChannelOption<Boolean> TCP_NODELAY =
            new ChannelOption<Boolean>("TCP_NODELAY");

    public static final ChannelOption<Boolean> SCTP_DISABLE_FRAGMENTS =
            new ChannelOption<Boolean>("SCTP_DISABLE_FRAGMENTS");
    public static final ChannelOption<Boolean> SCTP_EXPLICIT_COMPLETE =
            new ChannelOption<Boolean>("SCTP_EXPLICIT_COMPLETE");
    public static final ChannelOption<Integer> SCTP_FRAGMENT_INTERLEAVE =
            new ChannelOption<Integer>("SCTP_FRAGMENT_INTERLEAVE");
    public static final ChannelOption<List<Integer>> SCTP_INIT_MAXSTREAMS =
            new ChannelOption<List<Integer>>("SCTP_INIT_MAXSTREAMS") {
        @Override
        public void validate(List<Integer> value) {
            super.validate(value);
            if (value.size() != 2) {
                throw new IllegalArgumentException("value must be a List of 2 Integers: " + value);
            }
            if (value.get(0) == null) {
                throw new NullPointerException("value[0]");
            }
            if (value.get(1) == null) {
                throw new NullPointerException("value[1]");
            }
        }
    };
    public static final ChannelOption<Boolean> SCTP_NODELAY =
            new ChannelOption<Boolean>("SCTP_NODELAY");
    public static final ChannelOption<SocketAddress> SCTP_PRIMARY_ADDR =
            new ChannelOption<SocketAddress>("SCTP_PRIMARY_ADDR");
    public static final ChannelOption<SocketAddress> SCTP_SET_PEER_PRIMARY_ADDR =
            new ChannelOption<SocketAddress>("SCTP_SET_PEER_PRIMARY_ADDR");

    public static final ChannelOption<Long> AIO_READ_TIMEOUT =
            new ChannelOption<Long>("AIO_READ_TIMEOUT");
    public static final ChannelOption<Long> AIO_WRITE_TIMEOUT =
            new ChannelOption<Long>("AIO_WRITE_TIMEOUT");

    /**
     * Create a new {@link ChannelOption} with the given name. The name needs to be
     * unique.
     *
     */
    public ChannelOption(String name) {
        super(names, name);
    }

    /**
     * Validate the value which is set for the {@link ChannelOption}. Sub-classes
     * may override this for special checks.
     *
     */
    public void validate(T value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
    }
}
