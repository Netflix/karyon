package com.netflix.karyon.server.http;

import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * An {@link HttpServer} using netty's {@link NioServerSocketChannel} i.e. non-blocking I/O. <br/>
 *
 * <h2>Blocking routers</h2>
 *
 * If the configured {@link HttpRequestRouter} is blocking then it is invoked in a different thread pool
 * (netty's event executor). <br/>
 *
 * <h2>Non-blocking routers</h2>
 *
 * If the configured {@link HttpRequestRouter} is non-blocking then it is invoked in the event loop. <br/>
 *
 * <h2>Architecture</h2>
 *  See {@link com.netflix.karyon.server.http} for architecture details.
 *
 * {@link NonBlockingHttpServerBuilder} is an easy way to create an instance of {@link NonBlockingHttpServer}
 *
 * @see com.netflix.karyon.server.http
 *
 * @author Nitesh Kant
 */
public class NonBlockingHttpServer<I extends HttpObject, O extends HttpObject> extends HttpServer<I, O> {

    public NonBlockingHttpServer(@Nonnull ServerBootstrap bootstrap,
                                 @Nonnull ResponseWriterFactory<O> responseWriterFactory,
                                 @Nullable PipelineFactory<I, O> interceptorFactory,
                                 @Nonnull ChannelPipelineConfigurator<I, O> pipelineConfigurator,
                                 @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        super(bootstrap, pipelineConfigurator, responseWriterFactory, interceptorFactory, karyonBootstrap);
    }

    @Override
    protected boolean shouldRunBlockingRouterInAnExecutor() {
        return true;
    }
}
