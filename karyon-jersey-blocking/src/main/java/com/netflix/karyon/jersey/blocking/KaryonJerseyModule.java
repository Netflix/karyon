package com.netflix.karyon.jersey.blocking;

import com.netflix.karyon.transport.http.KaryonHttpModule;
import io.netty.buffer.ByteBuf;

/**
 * @author Nitesh Kant
 */
public abstract class KaryonJerseyModule extends KaryonHttpModule<ByteBuf, ByteBuf> {

    public KaryonJerseyModule() {
        super("karyonJerseyModule", ByteBuf.class, ByteBuf.class);
    }

    protected KaryonJerseyModule(String moduleName) {
        super(moduleName, ByteBuf.class, ByteBuf.class);
    }

    @Override
    protected void configure() {
        bindRouter().to(JerseyBasedRouter.class);
        super.configure();
    }
}
