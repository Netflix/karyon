package com.netflix.karyon.jersey.blocking;

import com.google.inject.binder.AnnotatedBindingBuilder;
import com.netflix.karyon.transport.http.AbstractHttpModule;
import com.netflix.karyon.transport.http.HttpRequestHandler;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import rx.internal.util.RxThreadFactory;

/**
 * @author Nitesh Kant
 */
public abstract class KaryonJerseyModule extends AbstractHttpModule<ByteBuf, ByteBuf> {

    public KaryonJerseyModule() {
        super(ByteBuf.class, ByteBuf.class);
    }

    @Override
    protected void bindRequestRouter(AnnotatedBindingBuilder<HttpRequestRouter<ByteBuf, ByteBuf>> bind) {
        bind.to(JerseyBasedRouter.class);
    }

    @Override
    protected HttpServerBuilder<ByteBuf, ByteBuf> newServerBuilder(int port,
                                                                   HttpRequestHandler<ByteBuf, ByteBuf> requestHandler) {
        return configureRequestProcessingThreads(super.newServerBuilder(port, requestHandler));
    }

    protected HttpServerBuilder<ByteBuf, ByteBuf> configureRequestProcessingThreads(HttpServerBuilder<ByteBuf, ByteBuf> builder) {
        return builder.withRequestProcessingThreads(requestProcessingThreadsCount(),
                                                    new RxThreadFactory("karyon-jersey-pool"));
    }

    protected int requestProcessingThreadsCount() {
        return 200;
    }
}
