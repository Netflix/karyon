package com.netflix.karyon.server.http.jersey.blocking;

import com.google.inject.binder.AnnotatedBindingBuilder;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.transport.http.AbstractHttpModule;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.netty.buffer.ByteBuf;

import javax.inject.Inject;

/**
 * @author Nitesh Kant
 */
public abstract class KaryonJerseyModule extends AbstractHttpModule<ByteBuf, ByteBuf> {

    private final KaryonBootstrap karyonBootstrap;

    @Inject
    public KaryonJerseyModule(KaryonBootstrap karyonBootstrap) {
        super(ByteBuf.class, ByteBuf.class);
        this.karyonBootstrap = karyonBootstrap;
    }

    @Override
    protected void bindRequestRouter(AnnotatedBindingBuilder<HttpRequestRouter<ByteBuf, ByteBuf>> bind) {
        bind.to(JerseyBasedRouter.class);
    }

    @Override
    public int serverPort() {
        return karyonBootstrap.port();
    }

    @Override
    public int shutdownPort() {
        return karyonBootstrap.shutdownPort();
    }
}
