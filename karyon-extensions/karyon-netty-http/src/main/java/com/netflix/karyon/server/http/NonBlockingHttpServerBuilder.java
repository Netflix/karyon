package com.netflix.karyon.server.http;

import com.netflix.karyon.server.NonBlockingServerBuilderAttributes;
import com.netflix.karyon.server.NonBlockingServerBuilderAttributesImpl;
import com.netflix.karyon.server.spi.BlockingRequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.logging.LogLevel;

import javax.annotation.Nullable;

/**
 * A builder to create a {@link NonBlockingHttpServer} instance. <br/>
 *
 * <h2>Event loops</h2>
 *
 * Every HttpServer requires two event loop groups, one for accepting new connections and another for serving a
 * particular client connection. This builder uses the same event loop group for both these event loops.<br/>
 * Since, this server only serves a single server port, there is always a single event loop required for accepting
 * new client connections. So, there is hardly ever a reason to specify more than one event loop for accepting new
 * connections & hence this builder does not allow configuring one.
 *
 * @see com.netflix.karyon.server.http
 *
 * @author Nitesh Kant
 */
public class NonBlockingHttpServerBuilder<I extends HttpObject, O extends HttpObject>
            extends HttpServerBuilder<NonBlockingHttpServerBuilder<I, O>, NonBlockingHttpServer<I, O>, I, O>
            implements NonBlockingServerBuilderAttributes<NonBlockingHttpServerBuilder<I, O>, I, O> {

    private final NonBlockingServerBuilderAttributesImpl<NonBlockingHttpServerBuilder<I, O>, I, O> nonBlockingServerBuilderAttributes;

    public NonBlockingHttpServerBuilder(int serverPort) {
        this(serverPort, null);
    }

    public NonBlockingHttpServerBuilder(int serverPort,  @Nullable LogLevel nettyLoggerLevel) {
        super(serverPort, nettyLoggerLevel);
        nonBlockingServerBuilderAttributes =
                new NonBlockingServerBuilderAttributesImpl<NonBlockingHttpServerBuilder<I, O>, I, O>(this, pipelineConfigurator);
    }

    @Override
    public NonBlockingHttpServerBuilder<I, O> withSelectorCount(int selectorCount) {
        return nonBlockingServerBuilderAttributes.withSelectorCount(selectorCount);
    }

    @Override
    public NonBlockingHttpServerBuilder<I, O> requestRouter(BlockingRequestRouter<I, O> requestRouter,
                                                            int executorThreadCount) {
        return nonBlockingServerBuilderAttributes.requestRouter(requestRouter, executorThreadCount);
    }

    @Override
    protected void validate() {
        super.validate();
        nonBlockingServerBuilderAttributes.validate();
    }

    @Override
    protected NonBlockingHttpServer<I, O> createServer() {
        return new NonBlockingHttpServer<I, O>(nettyBootstrap, responseWriterFactory, interceptorFactory,
                                               pipelineConfigurator, karyonBootstrap);
    }

    @Override
    protected void configureNettyBootstrap(ServerBootstrap nettyBootstrap) {
        nonBlockingServerBuilderAttributes.configurNIOattributesInBootstrap(nettyBootstrap);
    }
}
