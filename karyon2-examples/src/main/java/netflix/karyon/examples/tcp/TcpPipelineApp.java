package netflix.karyon.examples.tcp;

import com.google.inject.AbstractModule;
import com.netflix.governator.annotations.Modules;
import io.netty.buffer.ByteBuf;
import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusBootstrap;
import netflix.karyon.examples.tcp.TcpPipelineApp.ApplicationModule;
import netflix.karyon.examples.tcp.TcpPipelineApp.TcpBackendModule;
import netflix.karyon.examples.tcp.TcpPipelineApp.TcpFrontendModule;
import netflix.karyon.transport.tcp.KaryonTcpModule;

/**
 * @author Tomasz Bak
 */
@ArchaiusBootstrap
@KaryonBootstrap(name = "sample-rxnetty-tcp-noss")
@Modules(include = {
        ShutdownModule.class,
        KaryonWebAdminModule.class,
        // KaryonEurekaModule.class, // Uncomment this to enable Eureka client.
        ApplicationModule.class,
        TcpFrontendModule.class, TcpBackendModule.class
})
public interface TcpPipelineApp {

    class ApplicationModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TcpPipelineHandlers.QueueProvider.class).asEagerSingleton();
        }
    }

    class TcpFrontendModule extends KaryonTcpModule<ByteBuf, ByteBuf> {
        public TcpFrontendModule() {
            super("tcpFrontServer", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindConnectionHandler().to(TcpPipelineHandlers.FrontendConnectionHandler.class);
            server().port(7770);
        }
    }

    class TcpBackendModule extends KaryonTcpModule<ByteBuf, ByteBuf> {
        public TcpBackendModule() {
            super("tcpBackendServer", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindConnectionHandler().to(TcpPipelineHandlers.BackendConnectionHandler.class);
            server().port(7771);
        }
    }
}
