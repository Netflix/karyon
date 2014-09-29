package com.netflix.karyon.examples.tcp;

import com.google.inject.AbstractModule;
import com.netflix.adminresources.resources.KaryonWebAdminModule;
import com.netflix.governator.annotations.Modules;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.ShutdownModule;
import com.netflix.karyon.archaius.ArchaiusBootstrap;
import com.netflix.karyon.examples.tcp.TcpPipelineApp.ApplicationModule;
import com.netflix.karyon.examples.tcp.TcpPipelineApp.TcpBackendModule;
import com.netflix.karyon.examples.tcp.TcpPipelineApp.TcpFrontendModule;
import com.netflix.karyon.examples.tcp.TcpPipelineHandlers.QueueProvider;
import com.netflix.karyon.transport.tcp.KaryonTcpModule;
import io.netty.buffer.ByteBuf;

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
            bind(QueueProvider.class).asEagerSingleton();
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
