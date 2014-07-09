package com.netflix.karyon.jersey.blocking;

import com.google.inject.binder.AnnotatedBindingBuilder;
import com.netflix.karyon.transport.http.AbstractHttpModule;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.netty.buffer.ByteBuf;

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
}
