package com.netflix.karyon.server.http;

import com.netflix.karyon.server.ApplicationPipelineConfigurator;
import com.netflix.karyon.server.KaryonNettyServer;
import com.netflix.karyon.server.bootstrap.KaryonBootstrap;
import com.netflix.karyon.server.http.interceptor.InterceptorsNettyHandler;
import com.netflix.karyon.server.http.interceptor.PipelineFactory;
import com.netflix.karyon.server.spi.ChannelPipelineConfigurator;
import com.netflix.karyon.server.spi.ResponseWriter;
import com.netflix.karyon.server.spi.ResponseWriterFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A base class for all HTTP servers based on {@link KaryonNettyServer}
 *
 * @param <I> Request type for the server.
 * @param <O> Response type for the server.
 */
public abstract class HttpServer<I extends HttpObject, O extends HttpObject> extends KaryonNettyServer<I, O> {

    @Nullable
    private final PipelineFactory<I, O> interceptorFactory;

    HttpServer(@Nonnull ServerBootstrap bootstrap,
               @Nonnull ChannelPipelineConfigurator<I, O> pipelineConfigurator,
               @Nonnull ResponseWriterFactory<O> responseWriterFactory,
               @Nullable final PipelineFactory<I, O> interceptorFactory,
               @Nullable KaryonBootstrap karyonBootstrap) {
        super(bootstrap, pipelineConfigurator, responseWriterFactory, karyonBootstrap);
        this.interceptorFactory = interceptorFactory;
    }

    @Override
    protected ApplicationPipelineConfigurator<I, O> newApplicationPipelineConfigurator() {
        return new ApplicationPipelineConfigurator<I, O>(routerExecutorGroup, responseWriterFactory, router,
                                                         taskRegistry, inputType) {
            @Override
            protected void configurePipeline(@SuppressWarnings("unused") ChannelPipeline channelPipeline,
                                             @SuppressWarnings("unused") ResponseWriter<O> responseWriter) {
                channelPipeline.addBefore(ROUTING_HANDLER_NAME,
                                          InterceptorsNettyHandler.INBOUND_INTERCEPTOR_NETTY_HANDLER,
                                          new InterceptorsNettyHandler<I, O>(interceptorFactory, responseWriter,
                                                                             inputType, outputType));
            }
        };
    }
}
