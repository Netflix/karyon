package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.filter.Filter;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.filter.FilterPipelineBuilder;
import com.netflix.karyon.server.http.filter.PipelineFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

/**
 * A builder that provides multiple configuration hooks to the {@link HttpServer}. <br/>
 *
 * <b>This class is not threadsafe.</b>
 *
 * @author Nitesh Kant
 */
public class HttpServerBuilder {

    private static final int NOT_DEFINED = -1;

    private final ServerBootstrap nettyBootstrap;
    private EventLoopGroup acceptorGroup;
    private EventLoopGroup workerGroup;
    private com.netflix.karyon.server.ServerBootstrap karyonBootstrap;
    private HttpRequestRouter requestRouter;
    private int executorThreadCount = NOT_DEFINED;
    private Class<? extends ServerChannel> serverChannelClass = NioServerSocketChannel.class;
    private int acceptorThreadCount;
    private int workerSelectorCount;
    private PipelineFactory filterFactory;
    private FilterPipelineBuilder filterPipelineBuilder;

    public HttpServerBuilder(int serverPort) {
        nettyBootstrap = new ServerBootstrap();
        nettyBootstrap.localAddress(serverPort);
    }

    public HttpServerBuilder withAcceptorThreads(int acceptorThreads) {
        acceptorThreadCount = acceptorThreads;
        return this;
    }

    public HttpServerBuilder withWorkerSelector(int selectorCount) {
        workerSelectorCount = selectorCount;
        return this;
    }

    public <T> HttpServerBuilder serverSocketOption(ChannelOption<T> channelOption, T value) {
        nettyBootstrap.option(channelOption, value);
        return this;
    }

    public <T> HttpServerBuilder clientSocketOption(ChannelOption<T> channelOption, T value) {
        nettyBootstrap.childOption(channelOption, value);
        return this;
    }

    public HttpServerBuilder blocking() {
        serverChannelClass = OioServerSocketChannel.class;
        return this;
    }

    public HttpServerBuilder requestRouter(HttpRequestRouter requestRouter) {
        this.requestRouter = requestRouter;
        return this;
    }

    public HttpServerBuilder filters(PipelineFactory filterFactory) {
        Preconditions.checkNotNull(filterFactory, "Filter factory can not be null.");
        this.filterFactory = filterFactory;
        return this;
    }

    public HttpServerBuilder filter(String constraint, Filter filter) {
        if (null == filterPipelineBuilder) {
            filterPipelineBuilder = new FilterPipelineBuilder();
        }

        filterPipelineBuilder.filter(constraint, filter);
        return this;
    }

    public HttpServerBuilder requestRouter(HttpRequestRouter requestRouter, int executorThreadCount) {
        this.requestRouter = requestRouter;
        this.executorThreadCount = executorThreadCount;
        return this;
    }

    public HttpServerBuilder karyonBootstrap(com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        this.karyonBootstrap = karyonBootstrap;
        return this;
    }

    public HttpServer build() {
        Preconditions.checkState(null != requestRouter, "Request router is not set.");
        Preconditions.checkState(requestRouter.isBlocking() && executorThreadCount != NOT_DEFINED,
                                 "Blocking request router must specify executor thread count.");
        boolean isBlocking = serverChannelClass == OioServerSocketChannel.class;
        if (isBlocking) {
            acceptorGroup = new OioEventLoopGroup(acceptorThreadCount);
            workerGroup = new OioEventLoopGroup(workerSelectorCount);
        } else{
            acceptorGroup = new NioEventLoopGroup(acceptorThreadCount);
            workerGroup = new NioEventLoopGroup(workerSelectorCount);
        }
        nettyBootstrap.group(acceptorGroup, workerGroup)
                      .channel(serverChannelClass);

        if (null == filterFactory && null != filterPipelineBuilder) {
            filterFactory = filterPipelineBuilder.buildFactory();
        }

        if (isBlocking) {
            return new BlockingHttpServer(nettyBootstrap, requestRouter, filterFactory, karyonBootstrap);
        } else {
            return new NonBlockingHttpServer(nettyBootstrap, requestRouter, executorThreadCount, filterFactory,
                                             karyonBootstrap);
        }
    }
}
