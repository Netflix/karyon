package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.interceptor.Interceptor;
import com.netflix.karyon.server.http.interceptor.InterceptorPipelineBuilder;
import com.netflix.karyon.server.http.interceptor.MethodConstraintKey;
import com.netflix.karyon.server.http.interceptor.PipelineDefinition;
import com.netflix.karyon.server.http.interceptor.UriConstraintKey;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

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
    private int acceptorThreadCount = 1;
    private int workerSelectorCount;
    private PipelineFactory interceptorFactory;
    private final List<InterceptorAttacher> interceptorAttachers;

    public HttpServerBuilder(int serverPort) {
        nettyBootstrap = new ServerBootstrap();
        nettyBootstrap.localAddress(serverPort);
        interceptorAttachers = new ArrayList<InterceptorAttacher>();
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

    public HttpServerBuilder interceptors(PipelineFactory interceptorFactory) {
        Preconditions.checkNotNull(interceptorFactory, "Interceptor pipeline factory can not be null.");
        this.interceptorFactory = interceptorFactory;
        return this;
    }

    public InterceptorAttacher forUri(String uri) {
        InterceptorAttacher interceptorAttacher = new InterceptorAttacher(new UriConstraintKey(uri));
        interceptorAttachers.add(interceptorAttacher);
        return interceptorAttacher;
    }

    public InterceptorAttacher forHttpMethod(HttpMethod method) {
        InterceptorAttacher interceptorAttacher = new InterceptorAttacher(new MethodConstraintKey(method));
        interceptorAttachers.add(interceptorAttacher);
        return interceptorAttacher;
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

        if (null == interceptorFactory && !interceptorAttachers.isEmpty()) {
            InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
            for (InterceptorAttacher interceptorAttacher : interceptorAttachers) {
                builder.addIntereceptorMapping(interceptorAttacher.constraint, interceptorAttacher.interceptors);
            }
            interceptorFactory = builder.buildFactory();
        }

        if (isBlocking) {
            return new BlockingHttpServer(nettyBootstrap, requestRouter, interceptorFactory, karyonBootstrap);
        } else {
            return new NonBlockingHttpServer(nettyBootstrap, requestRouter, executorThreadCount, interceptorFactory,
                                             karyonBootstrap);
        }
    }

    public class InterceptorAttacher {

        private final PipelineDefinition.Key constraint;
        private Interceptor[] interceptors;

        public InterceptorAttacher(PipelineDefinition.Key constraint) {
            this.constraint = constraint;
        }

        public HttpServerBuilder interceptWith(Interceptor... interceptors) {
            this.interceptors = interceptors;
            return HttpServerBuilder.this;
        }
    }
}
