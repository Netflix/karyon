package io.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;

import java.net.SocketAddress;

/**
 * Temporary class to by-pass the issue with {@link ChannelHandlerContext} which exposes a package-private class.
 * See this <a href="https://github.com/netty/netty/issues/1991">netty issue</a> for details.
 *
 * @author Nitesh Kant
 */
public class ChannelHandlerContextWrapper implements ChannelHandlerContext {

    @Override
    public Channel channel() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public EventExecutor executor() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public String name() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandler handler() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public boolean isRemoved() {
        // TODO: Auto-generated method stub
        return false;
    }

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    @Deprecated
    public ChannelHandlerContext fireChannelUnregistered() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandlerContext fireChannelActive() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandlerContext fireChannelInactive() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandlerContext fireUserEventTriggered(Object event) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandlerContext fireChannelRead(Object msg) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandlerContext fireChannelWritabilityChanged() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelHandlerContext flush() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture disconnect() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture close() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    @Deprecated
    public ChannelFuture deregister() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress,
                                 ChannelPromise promise) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    @Deprecated
    public ChannelFuture deregister(ChannelPromise promise) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelOutboundInvoker read() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture write(Object msg) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelPipeline pipeline() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ByteBufAllocator alloc() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelPromise newPromise() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        // TODO: Auto-generated method stub
        return null;
    }

    @Override
    public ChannelPromise voidPromise() {
        // TODO: Auto-generated method stub
        return null;
    }
}
