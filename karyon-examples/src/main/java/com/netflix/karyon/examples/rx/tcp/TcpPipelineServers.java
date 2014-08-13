package com.netflix.karyon.examples.rx.tcp;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.netflix.governator.annotations.Modules;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.Submodules;
import com.netflix.karyon.archaius.ArchaiusBootstrap;
import com.netflix.karyon.examples.hellonoss.server.health.HealthCheck;
import com.netflix.karyon.examples.rx.tcp.TcpPipelineHandlers.BackendConnectionHandler;
import com.netflix.karyon.examples.rx.tcp.TcpPipelineHandlers.FrontendConnectionHandler;
import com.netflix.karyon.examples.rx.tcp.TcpPipelineHandlers.QueueProvider;
import com.netflix.karyon.examples.rx.tcp.TcpPipelineServers.ApplicationModule;
import com.netflix.karyon.examples.rx.tcp.TcpPipelineServers.TcpBackendModule;
import com.netflix.karyon.examples.rx.tcp.TcpPipelineServers.TcpFrontendModule;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import com.netflix.karyon.transport.ServerPort;
import com.netflix.karyon.transport.tcp.TcpRxNettyModule;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.server.ServerMetricsEvent;
import io.reactivex.netty.server.ServerMetricsEvent.EventType;

/**
 * @author Tomasz Bak
 */
@ArchaiusBootstrap
@KaryonBootstrap(name = "sample-rxnetty-tcp-noss", healthcheck = HealthCheck.class)
@Modules(include = ApplicationModule.class)
@Submodules(
        include = {
                TcpFrontendModule.class,
                TcpBackendModule.class
        }
)
public interface TcpPipelineServers {

    class TcpFrontendModule extends TcpRxNettyModule<ByteBuf, ByteBuf> {
        public TcpFrontendModule() {
            super(ByteBuf.class, ByteBuf.class,
                    Types.newParameterizedTypeWithOwner(TcpPipelineHandlers.class, FrontendConnectionHandler.class));
        }

        @Override
        public int serverPort() {
            return 9988;
        }

        @Override
        public MetricEventsListenerFactory<ByteBuf, ByteBuf, ServerMetricsEvent<EventType>> metricsEventsListenerFactory() {
            return new MetricEventsListenerFactory.TcpMetricEventsListenerFactory<ByteBuf, ByteBuf>();
        }
    }

    class TcpBackendModule extends TcpRxNettyModule<ByteBuf, ByteBuf> {
        public TcpBackendModule() {
            super(ByteBuf.class, ByteBuf.class,
                    Types.newParameterizedTypeWithOwner(TcpPipelineHandlers.class, BackendConnectionHandler.class));
        }

        @Override
        public int serverPort() {
            return 9989;
        }

        @Override
        public MetricEventsListenerFactory<ByteBuf, ByteBuf, ServerMetricsEvent<EventType>> metricsEventsListenerFactory() {
            return new MetricEventsListenerFactory.TcpMetricEventsListenerFactory<ByteBuf, ByteBuf>();
        }
    }

    class ApplicationModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ServerPort.class).annotatedWith(Names.named("shutdown")).toInstance(new ServerPort(5555));

            bind(QueueProvider.class).asEagerSingleton();
            bind(FrontendConnectionHandler.class);
            bind(BackendConnectionHandler.class);
        }
    }
}
