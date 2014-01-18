package com.netflix.karyon.server.http;

import com.netflix.karyon.server.BlockingServerBuilderAttributes;
import com.netflix.karyon.server.BlockingServerBuilderAttributesImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.logging.LogLevel;

import javax.annotation.Nullable;

/**
 * A builder to create a {@link BlockingHttpServer} instance. <br/>
 *
 * <h2>Event loops</h2>
 *
 * Every HttpServer requires two event loop groups, one for accepting new connections and another for serving a
 * particular client connection. By default, this builder uses the same event loop group for both these event loops.<br/>
 * Since, this server only serves a single server port, there is always a single event loop required for accepting
 * new client connections. So, there is hardly ever a reason to specify more than one event loop for accepting new
 * connections & hence this builder does not allow configuring one.
 *
 * @see com.netflix.karyon.server.http
 *
 * @author Nitesh Kant
 */
public class BlockingHttpServerBuilder<I extends HttpObject, O extends HttpObject>
        extends HttpServerBuilder<BlockingHttpServerBuilder<I, O>, BlockingHttpServer<I, O>, I, O>
        implements BlockingServerBuilderAttributes<BlockingHttpServerBuilder<I, O>>{

    private final BlockingServerBuilderAttributesImpl<BlockingHttpServerBuilder<I, O>> blockingServerBuilderAttributes;

    public BlockingHttpServerBuilder(int serverPort) {
        this(serverPort, null);
    }

    public BlockingHttpServerBuilder(int serverPort, @Nullable LogLevel nettyLoggerLevel) {
        super(serverPort, nettyLoggerLevel);
        blockingServerBuilderAttributes = new BlockingServerBuilderAttributesImpl<BlockingHttpServerBuilder<I, O>>(this);
    }

    @Override
    protected BlockingHttpServer<I, O> createServer() {
        return new BlockingHttpServer<I, O>(nettyBootstrap, interceptorFactory, pipelineConfigurator,
                                            responseWriterFactory, karyonBootstrap);
    }

    @Override
    protected void configureNettyBootstrap(ServerBootstrap nettyBootstrap) {
        blockingServerBuilderAttributes.configurOIOattributesInBootstrap(nettyBootstrap);
    }

    @Override
    public BlockingHttpServerBuilder<I, O> withWorkerCount(int workerCount) {
        return blockingServerBuilderAttributes.withWorkerCount(workerCount);
    }
}
