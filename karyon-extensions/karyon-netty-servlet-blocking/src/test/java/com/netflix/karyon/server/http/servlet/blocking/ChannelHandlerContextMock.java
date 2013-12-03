package com.netflix.karyon.server.http.servlet.blocking;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContextWrapper;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.DefaultAttributeMap;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Nitesh Kant
 */
public class ChannelHandlerContextMock extends ChannelHandlerContextWrapper {

    private final Channel channel;
    private final InetSocketAddress serverSockAddr;
    private final InetSocketAddress remoteAddr;
    private final InetSocketAddress localAddr;
    private final DefaultAttributeMap attributeMap;

    public ChannelHandlerContextMock(final String serverAddr, final int serverPort,
                                     final String localAddr, final int localPort,
                                     final String remoteAddr, final int remotePort) {
        serverSockAddr = InetSocketAddress.createUnresolved(serverAddr, serverPort);
        this.localAddr = new InetSocketAddress(localAddr, localPort);
        this.remoteAddr = new InetSocketAddress(remoteAddr, remotePort);
        final ServerSocketChannel parent = new OioServerSocketChannel() {
            @Override
            protected SocketAddress localAddress0() {
                return serverSockAddr;
            }
        };
        channel = new OioSocketChannel() {
            @Override
            public ServerSocketChannel parent() {
                return parent;
            }

            @Override
            public InetSocketAddress localAddress() {
                return ChannelHandlerContextMock.this.localAddr;
            }

            @Override
            public InetSocketAddress remoteAddress() {
                return ChannelHandlerContextMock.this.remoteAddr;
            }
        };
        attributeMap = new DefaultAttributeMap();
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return attributeMap.attr(key);
    }
}
