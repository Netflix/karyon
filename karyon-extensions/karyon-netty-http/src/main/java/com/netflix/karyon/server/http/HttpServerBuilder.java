package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.interceptor.BidirectionalInterceptorAdapter;
import com.netflix.karyon.server.http.interceptor.InboundInterceptor;
import com.netflix.karyon.server.http.interceptor.InterceptorPipelineBuilder;
import com.netflix.karyon.server.http.interceptor.MethodConstraintKey;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.http.interceptor.PipelineDefinition;
import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.http.interceptor.UriConstraintKey;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nitesh Kant
 */
@SuppressWarnings("rawtypes")
public abstract class HttpServerBuilder<T extends HttpServerBuilder, S extends HttpServer> {

    protected final ServerBootstrap nettyBootstrap;
    protected final List<InterceptorAttacher> interceptorAttachers;
    protected com.netflix.karyon.server.ServerBootstrap karyonBootstrap;
    protected PipelineFactory interceptorFactory;
    protected HttpRequestRouter requestRouter;

    protected HttpServerBuilder(int serverPort) {
        nettyBootstrap = new ServerBootstrap();
        nettyBootstrap.localAddress(serverPort);
        interceptorAttachers = new ArrayList<InterceptorAttacher>();
    }

    public T interceptors(PipelineFactory interceptorFactory) {
        Preconditions.checkNotNull(interceptorFactory, "Interceptor pipeline factory can not be null.");
        this.interceptorFactory = interceptorFactory;
        return returnBuilder();
    }

    public <O> T serverSocketOption(ChannelOption<O> channelOption, O value) {
        nettyBootstrap.option(channelOption, value);
        return returnBuilder();
    }

    public <O> T clientSocketOption(ChannelOption<O> channelOption, O value) {
        nettyBootstrap.childOption(channelOption, value);
        return returnBuilder();
    }

    public T requestRouter(HttpRequestRouter requestRouter) {
        this.requestRouter = requestRouter;
        return returnBuilder();
    }

    public InterceptorAttacher forUri(String uri) {
        Preconditions.checkNotNull(uri, "Uri for intercepting must not be null");
        InterceptorAttacher interceptorAttacher = new InterceptorAttacher(new UriConstraintKey(uri));
        interceptorAttachers.add(interceptorAttacher);
        return interceptorAttacher;
    }

    public InterceptorAttacher forHttpMethod(HttpMethod method) {
        Preconditions.checkNotNull(method, "Http method for intercepting must not be null");
        InterceptorAttacher interceptorAttacher = new InterceptorAttacher(new MethodConstraintKey(method));
        interceptorAttachers.add(interceptorAttacher);
        return interceptorAttacher;
    }

    public T karyonBootstrap(com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        this.karyonBootstrap = karyonBootstrap;
        return returnBuilder();
    }

    public S build() {
        validate();
        buildInterceptorFactory();
        configureBootstrap();
        return createServer();
    }

    protected abstract S createServer();

    protected abstract void configureBootstrap();

    @SuppressWarnings("unchecked")
    private T returnBuilder() {
        return (T) this;
    }

    protected void validate() {
        Preconditions.checkState(null != requestRouter, "Request router is not set.");
    }

    protected void buildInterceptorFactory() {
        if (null == interceptorFactory && !interceptorAttachers.isEmpty()) {
            InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
            for (InterceptorAttacher interceptorAttacher : interceptorAttachers) {
                if (null != interceptorAttacher.inboundInterceptors && interceptorAttacher.inboundInterceptors.length != 0) {
                    builder.addIntereceptorMapping(interceptorAttacher.constraint, interceptorAttacher.inboundInterceptors);
                }
                if (null != interceptorAttacher.outboundInterceptors && interceptorAttacher.outboundInterceptors.length != 0) {
                    builder.addIntereceptorMapping(interceptorAttacher.constraint, interceptorAttacher.outboundInterceptors);
                }
                if (null != interceptorAttacher.bidirectionalInterceptors && interceptorAttacher.bidirectionalInterceptors.length != 0) {
                    builder.addIntereceptorMapping(interceptorAttacher.constraint, interceptorAttacher.bidirectionalInterceptors);
                }
            }
            interceptorFactory = builder.buildFactory();
        }
    }

    public class InterceptorAttacher {

        private final PipelineDefinition.Key constraint;
        private InboundInterceptor[] inboundInterceptors;
        private OutboundInterceptor[] outboundInterceptors;
        private BidirectionalInterceptorAdapter[] bidirectionalInterceptors;

        public InterceptorAttacher(PipelineDefinition.Key constraint) {
            this.constraint = constraint;
        }

        public T interceptWith(InboundInterceptor... interceptors) {
            inboundInterceptors = interceptors;
            return returnBuilder();
        }

        public T interceptWith(OutboundInterceptor... interceptors) {
            outboundInterceptors = interceptors;
            return returnBuilder();
        }

        public T interceptWith(BidirectionalInterceptorAdapter... interceptors) {
            bidirectionalInterceptors = interceptors;
            return returnBuilder();
        }
    }
}
