package netflix.karyon.jersey.blocking;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.http.KaryonHttpModule;

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
