package com.netflix.karyon.transport;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.netflix.karyon.transport.AbstractServerModule.ServerConfigBuilder;
import io.reactivex.netty.metrics.MetricEventsListenerFactory;
import io.reactivex.netty.pipeline.PipelineConfigurator;

/**
 * @author Tomasz Bak
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractServerModule<I, O, B extends ServerConfigBuilder> extends AbstractModule {

    protected final Named nameAnnotation;
    protected final Class<I> iType;
    protected final Class<O> oType;

    protected final Key<PipelineConfigurator> pipelineConfiguratorKey;
    protected final Key<MetricEventsListenerFactory> metricEventsListenerFactoryKey;

    protected final Key<ServerConfig> serverConfigKey;
    protected final B serverConfigBuilder;

    protected AbstractServerModule(String moduleName, Class<I> iType, Class<O> oType) {
        nameAnnotation = Names.named(moduleName);
        this.iType = iType;
        this.oType = oType;

        pipelineConfiguratorKey = Key.get(PipelineConfigurator.class, nameAnnotation);
        metricEventsListenerFactoryKey = Key.get(MetricEventsListenerFactory.class, nameAnnotation);
        serverConfigKey = Key.get(ServerConfig.class, nameAnnotation);

        serverConfigBuilder = newServerConfigBuilder();
    }

    protected abstract void configureServer();

    protected abstract B newServerConfigBuilder();

    protected LinkedBindingBuilder<PipelineConfigurator> bindPipelineConfigurator() {
        return bind(pipelineConfiguratorKey);
    }

    protected LinkedBindingBuilder<MetricEventsListenerFactory> bindEventsListenerFactory() {
        return bind(metricEventsListenerFactoryKey);
    }

    protected B server() {
        return serverConfigBuilder;
    }

    public static class ServerConfig {
        private final int port;

        public ServerConfig(int port) {
            this.port = port;
        }

        public int getPort() {
            return port;
        }
    }

    public static class ServerConfigBuilder<B extends ServerConfigBuilder, C extends ServerConfig> {

        protected int port = 8080;

        public B port(int port) {
            this.port = port;
            return (B) this;
        }

        public C build() {
            return (C) new ServerConfig(port);
        }
    }
}
