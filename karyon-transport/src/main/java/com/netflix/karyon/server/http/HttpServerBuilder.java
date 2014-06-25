package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.KaryonNettyServerBuilder;
import com.netflix.karyon.server.bootstrap.KaryonBootstrap;
import com.netflix.karyon.server.http.interceptor.InboundInterceptor;
import com.netflix.karyon.server.http.interceptor.InterceptorPipelineBuilder;
import com.netflix.karyon.server.http.interceptor.MethodConstraintKey;
import com.netflix.karyon.server.http.interceptor.OutboundInterceptor;
import com.netflix.karyon.server.http.interceptor.PipelineDefinition;
import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.http.interceptor.RegexUriConstraintKey;
import com.netflix.karyon.server.http.interceptor.ServletStyleUriConstraintKey;
import com.netflix.karyon.server.spi.DefaultChannelPipelineConfigurator;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.logging.LogLevel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nitesh Kant
 */
@SuppressWarnings("rawtypes")
public abstract class HttpServerBuilder<B extends HttpServerBuilder, S extends HttpServer<I, O>, I extends HttpObject,
                                        O extends HttpObject> extends KaryonNettyServerBuilder<B, S, I, O> {

    protected final List<InterceptorAttacher<I, O>> interceptorAttachers;
    protected KaryonBootstrap karyonBootstrap;
    protected PipelineFactory<I, O> interceptorFactory;

    protected HttpServerBuilder(int serverPort, @Nullable LogLevel nettyLoggerLevel) {
        super(serverPort, nettyLoggerLevel);
        interceptorAttachers = new ArrayList<InterceptorAttacher<I, O>>();
        pipelineConfigurator(new DefaultChannelPipelineConfigurator<I, O>(createUniqueServerName(serverPort),
                                                                          null == nettyLoggerLevel
                                                                          ? LogLevel.DEBUG : nettyLoggerLevel,
                                                                          new HttpPipelineConfigurator()));
    }

    public B interceptors(PipelineFactory<I, O> interceptorFactory) {
        Preconditions.checkNotNull(interceptorFactory, "Interceptor pipeline factory can not be null.");
        this.interceptorFactory = interceptorFactory;
        return returnBuilder();
    }

    public InterceptorAttacher<I, O> forUri(String uri) {
        Preconditions.checkNotNull(uri, "Uri for intercepting must not be null");
        InterceptorAttacher<I, O> interceptorAttacher = new InterceptorAttacher<I, O>(new ServletStyleUriConstraintKey(uri, ""));
        interceptorAttachers.add(interceptorAttacher);
        return interceptorAttacher;
    }

    public InterceptorAttacher<I, O> forUriRegex(String regEx) {
        Preconditions.checkNotNull(regEx, "URI Regular expression for interception must not be null");
        InterceptorAttacher<I, O> interceptorAttacher = new InterceptorAttacher<I, O>(new RegexUriConstraintKey(regEx));
        interceptorAttachers.add(interceptorAttacher);
        return interceptorAttacher;
    }

    public InterceptorAttacher<I, O> forHttpMethod(HttpMethod method) {
        Preconditions.checkNotNull(method, "Http method for intercepting must not be null");
        InterceptorAttacher<I, O> interceptorAttacher = new InterceptorAttacher<I, O>(new MethodConstraintKey(method));
        interceptorAttachers.add(interceptorAttacher);
        return interceptorAttacher;
    }

    @Override
    public S build() {
        buildInterceptorFactory(); // This happens before validate in base class which is not ideal but does not hurt
                                   // & is better than creating stages in the build method itself.
        return super.build();
    }

    protected void buildInterceptorFactory() {
        if (null == interceptorFactory && !interceptorAttachers.isEmpty()) {
            InterceptorPipelineBuilder<I, O> builder = new InterceptorPipelineBuilder<I, O>();
            for (InterceptorAttacher<I, O> interceptorAttacher : interceptorAttachers) {
                if (null != interceptorAttacher.inboundInterceptors && interceptorAttacher.inboundInterceptors.length != 0) {
                    builder.addIntereceptorMapping(interceptorAttacher.constraint, interceptorAttacher.inboundInterceptors);
                }
                if (null != interceptorAttacher.outboundInterceptors && interceptorAttacher.outboundInterceptors.length != 0) {
                    builder.addIntereceptorMapping(interceptorAttacher.constraint, interceptorAttacher.outboundInterceptors);
                }
            }
            interceptorFactory = builder.buildFactory();
        }
    }

    public class InterceptorAttacher<I extends HttpObject, O extends HttpObject> {

        private final PipelineDefinition.Key constraint;
        private InboundInterceptor<I, O>[] inboundInterceptors;
        private OutboundInterceptor<O>[] outboundInterceptors;

        public InterceptorAttacher(PipelineDefinition.Key constraint) {
            this.constraint = constraint;
        }

        public B interceptWith(InboundInterceptor<I, O>... interceptors) {
            inboundInterceptors = interceptors;
            return returnBuilder();
        }

        public B interceptWith(OutboundInterceptor<O>... interceptors) {
            outboundInterceptors = interceptors;
            return returnBuilder();
        }
    }
}
