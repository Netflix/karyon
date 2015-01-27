package netflix.karyon.server;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelProgressivePromise;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.local.LocalChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;
import io.netty.util.DefaultAttributeMap;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.SocketAddress;

/**
 * @author Nitesh Kant
 */
public class MockChannelHandlerContext implements ChannelHandlerContext {

    private final Channel channel;
    private final String name;
    private final ChannelDuplexHandler handler;
    private final UnpooledByteBufAllocator bufAllocator = new UnpooledByteBufAllocator(true);
    private final AttributeMap attributeMap = new DefaultAttributeMap();

    public MockChannelHandlerContext(String name) {
        this(new LocalChannel(), name, null);
    }

    public MockChannelHandlerContext(Channel channel, String name, ChannelDuplexHandler handler) {
        this.channel = channel;
        this.name = name;
        this.handler = handler;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public EventExecutor executor() {
        return GlobalEventExecutor.INSTANCE;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ChannelHandler handler() {
        return handler;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public ChannelHandlerContext fireChannelRegistered() {
        try {
            if (null != handler) {
                handler.channelRegistered(this);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelUnregistered() {
        try {
            if (null != handler) {
                handler.channelUnregistered(this);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelActive() {
        try {
            if (null != handler) {
                handler.channelActive(this);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelInactive() {
        try {
            if (null != handler) {
                handler.channelInactive(this);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelHandlerContext fireExceptionCaught(Throwable cause) {
        try {
            if (null != handler) {
                handler.exceptionCaught(this, cause);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelHandlerContext fireUserEventTriggered(Object event) {
        try {
            if (null != handler) {
                handler.userEventTriggered(this, event);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelRead(Object msg) {
        try {
            if (null != handler) {
                handler.channelRead(this, msg);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelReadComplete() {
        try {
            if (null != handler) {
                handler.channelReadComplete(this);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelHandlerContext fireChannelWritabilityChanged() {
        try {
            if (null != handler) {
                handler.channelWritabilityChanged(this);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture disconnect() {
        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture close() {
        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture deregister() {
        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelHandlerContext read() {
        channel.read();
        return this;
    }

    @Override
    public ChannelFuture write(Object msg) {
        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        promise.setSuccess();
        return promise;
    }

    @Override
    public ChannelHandlerContext flush() {
        try {
            if (null != handler) {
                handler.flush(this);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        try {
            if (null != handler) {
                handler.write(this, msg, promise);
                handler.flush(this);
            }
        } catch (Exception e) {
            promise.tryFailure(e);
        }
        return promise;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        try {
            if (null != handler) {
                handler.write(this, msg, promise);
                handler.flush(this);
            }
        } catch (Exception e) {
            promise.tryFailure(e);
        }
        return promise;
    }

    @Override
    public ChannelPipeline pipeline() {
        return null;
    }

    @Override
    public ByteBufAllocator alloc() {
        return bufAllocator;
    }

    @Override
    public ChannelPromise newPromise() {
        return new DefaultChannelPromise(channel);
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return new DefaultChannelProgressivePromise(channel);
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return new DefaultChannelPromise(channel).setSuccess();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return new DefaultChannelPromise(channel).setFailure(cause);
    }

    @Override
    public ChannelPromise voidPromise() {
        return new DefaultChannelPromise(channel);
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return attributeMap.attr(key);
    }
}
