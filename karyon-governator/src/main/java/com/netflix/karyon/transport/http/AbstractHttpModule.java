package com.netflix.karyon.transport.http;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.util.Types;
import com.netflix.karyon.transport.KaryonTransport;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;
import rx.functions.Action1;

import java.lang.reflect.ParameterizedType;

/**
 * A guice module that provides helper methods to configure a Karyon server.
 *
 * @author Nitesh Kant
 */
public abstract class AbstractHttpModule<I, O> extends AbstractModule {

    private Class<I> iType;
    private Class<O> oType;

    protected AbstractHttpModule(Class<I> iType, Class<O> oType) {
        this.iType = iType;
        this.oType = oType;
    }

    @Override
    protected void configure() {
        int listenPort = serverPort();
        GovernatorHttpInterceptorSupport<I, O> interceptorSupport = new GovernatorHttpInterceptorSupport<I, O>();
        LazyDelegateRouterImpl<I, O> lazyRouter = new LazyDelegateRouterImpl<I, O>(interceptorSupport);
        final LazyHttpRequestHandler<I, O> requestHandler = new LazyHttpRequestHandler<I, O>(lazyRouter);
        interceptorSupport.setFinishListener(new Action1<GovernatorHttpInterceptorSupport<I, O>>() {
            @Override
            public void call(GovernatorHttpInterceptorSupport<I, O> interceptorSupport) {
                requestHandler.activate(interceptorSupport);
            }
        });

        ParameterizedType lazyRouterParametrizedType = Types.newParameterizedType(LazyDelegateRouter.class, iType, oType);
        @SuppressWarnings("unchecked")
        TypeLiteral<LazyDelegateRouter<I, O>> lazyRouterTypeLiteral = (TypeLiteral<LazyDelegateRouter<I, O>>) TypeLiteral.get(lazyRouterParametrizedType);
        bind(lazyRouterTypeLiteral).toInstance(lazyRouter);

        ParameterizedType routerParametrizedType = Types.newParameterizedType(HttpRequestRouter.class, iType, oType);
        @SuppressWarnings("unchecked")
        TypeLiteral<HttpRequestRouter<I, O>> routerTypeLiteral = (TypeLiteral<HttpRequestRouter<I, O>>) TypeLiteral.get(routerParametrizedType);
        bindRequestRouter(bind(routerTypeLiteral));

        HttpServerBuilder<I, O> serverBuilder = newServerBuilder(listenPort, requestHandler);

        configureEventLoops(serverBuilder);

        configureChannelOptions(serverBuilder);

        configurePipeline(serverBuilder);

        configureInterceptors(interceptorSupport);

        LogLevel logLevel = enableWireLogging();
        if (null != logLevel) {
            serverBuilder.enableWireLogging(logLevel);
        }

        ParameterizedType bootstrapParametrizedType = Types.newParameterizedType(ServerBootstrap.class, iType, oType);
        @SuppressWarnings("unchecked")
        TypeLiteral<ServerBootstrap<I, O>> bootstrapTypeLiteral = (TypeLiteral<ServerBootstrap<I, O>>) TypeLiteral.get(bootstrapParametrizedType);
        bind(bootstrapTypeLiteral).asEagerSingleton();

        ParameterizedType builderParametrizedType = Types.newParameterizedType(HttpServerBuilder.class, iType, oType);
        @SuppressWarnings("unchecked")
        TypeLiteral<HttpServerBuilder<I, O>> builderTypeLiteral = (TypeLiteral<HttpServerBuilder<I, O>>) TypeLiteral.get(builderParametrizedType);
        bind(builderTypeLiteral).toInstance(serverBuilder);
    }

    /**
     * Returns the port at which the transport server created by this module will listen.
     *
     * @return The port at which the transport server created by this module will listen.
     */
    public abstract int serverPort();

    public abstract int shutdownPort();

    protected abstract void bindRequestRouter(AnnotatedBindingBuilder<HttpRequestRouter<I,O>> bind);

    protected HttpServerBuilder<I, O> newServerBuilder(int port, HttpRequestHandler<I, O> requestHandler) {
        return KaryonTransport.newHttpServerBuilder(port, requestHandler);
    }

    public void configureChannelOptions(@SuppressWarnings("unused") HttpServerBuilder<I, O> serverBuilder) {
        // No Op by default.
    }

    public void configureEventLoops(@SuppressWarnings("unused") HttpServerBuilder<I, O> serverBuilder) {
        // No Op by default.
    }

    public void configurePipeline(@SuppressWarnings("unused") HttpServerBuilder<I, O> serverBuilder) {
        // No Op by default.
    }

    public LogLevel enableWireLogging() {
        return LogLevel.DEBUG; // Add the wire logging handler by default so that it can be turned on at runtime.
    }

    public void configureInterceptors(@SuppressWarnings("unused") GovernatorHttpInterceptorSupport<I, O> interceptorSupport) {
        // No op by default
    }

    private static class LazyHttpRequestHandler<I, O> extends HttpRequestHandler<I, O> {

        private final LazyDelegateRouterImpl<I, O> lazyRouter;
        private HttpRequestHandler<I, O> delegate;

        public LazyHttpRequestHandler(LazyDelegateRouterImpl<I, O> lazyRouter) {
            super(lazyRouter);
            this.lazyRouter = lazyRouter;
        }

        @Override
        public Observable<Void> handle(HttpServerRequest<I> request, HttpServerResponse<O> response) {
            if (null != delegate) {
                return delegate.handle(request, response);
            }
            return super.handle(request, response);
        }

        /*package private*/ void activate(GovernatorHttpInterceptorSupport<I, O> interceptorSupport) {
            delegate = new HttpRequestHandler<I, O>(lazyRouter, interceptorSupport);
        }
    }
}
