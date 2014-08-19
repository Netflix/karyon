package com.netflix.karyon.experimental;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.ProviderWithDependencies;
import com.netflix.karyon.experimental.ExpServerModule.ServerConfig;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.metrics.MetricEventsListenerFactory;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.server.RxServer;
import io.reactivex.netty.server.ServerBuilder;

/**
 * @author Tomasz Bak
 */
public class TcpRxServerProvider<I, O, S extends RxServer<I, O>> implements ProviderWithDependencies<S> {

    private final Named nameAnnotation;

    protected final Key<ConnectionHandler> connectionHandlerKey;
    private final Key<PipelineConfigurator> pipelineConfiguratorKey;
    private final Key<MetricEventsListenerFactory> metricEventsListenerFactoryKey;
    private final Key<ServerConfig> serverConfigKey;

    private final Set<Dependency<?>> deps;
    private RxServer<I, O> server;

    public TcpRxServerProvider(String name) {
        nameAnnotation = Names.named(name);

        connectionHandlerKey = Key.get(ConnectionHandler.class, nameAnnotation);

        pipelineConfiguratorKey = Key.get(PipelineConfigurator.class, nameAnnotation);
        metricEventsListenerFactoryKey = Key.get(MetricEventsListenerFactory.class, nameAnnotation);
        serverConfigKey = Key.get(ServerConfig.class, nameAnnotation);

        Set<Dependency<?>> deps = new HashSet<Dependency<?>>();
        Collections.addAll(deps,
                Dependency.get(connectionHandlerKey),
                Dependency.get(pipelineConfiguratorKey),
                Dependency.get(metricEventsListenerFactoryKey),
                Dependency.get(serverConfigKey)
        );
        this.deps = Collections.unmodifiableSet(deps);
    }

    @Override
    public Set<Dependency<?>> getDependencies() {
        return deps;
    }

    @Override
    public S get() {
        return (S) server;
    }

    @Inject
    public void setInjector(Injector injector) {
        ServerConfig config = injector.getInstance(serverConfigKey);

        ConnectionHandler<I, O> connectionHandler = injector.getInstance(connectionHandlerKey);

        ServerBuilder<I, O> builder = RxNetty.newTcpServerBuilder(config.getPort(), connectionHandler);

        if (injector.getExistingBinding(pipelineConfiguratorKey) != null) {
            builder.appendPipelineConfigurator(injector.getInstance(pipelineConfiguratorKey));
        }

        if (injector.getExistingBinding(metricEventsListenerFactoryKey) != null) {
            builder.withMetricEventsListenerFactory(injector.getInstance(metricEventsListenerFactoryKey));
        }

        server = builder.build();
    }
}
