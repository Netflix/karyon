package com.netflix.karyon.experimental;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.reactivex.netty.metrics.MetricEventsListenerFactory;
import io.reactivex.netty.pipeline.PipelineConfigurator;

/**
 * @author Tomasz Bak
 */
public abstract class ExpServerModule<I, O> extends AbstractModule {

    protected Named nameAnnotation;

    protected final Key<PipelineConfigurator> pipelineConfiguratorKey;
    protected final Key<MetricEventsListenerFactory> metricEventsListenerFactoryKey;

    protected final Key<ServerConfig> serverConfigKey;
    protected final ServerConfigBuilder serverConfigBuilder;

    protected ExpServerModule(String moduleName) {
        nameAnnotation = Names.named(moduleName);

        pipelineConfiguratorKey = Key.get(PipelineConfigurator.class, nameAnnotation);
        metricEventsListenerFactoryKey = Key.get(MetricEventsListenerFactory.class, nameAnnotation);
        serverConfigKey = Key.get(ServerConfig.class, nameAnnotation);

        serverConfigBuilder = new ServerConfigBuilder();
    }

    protected abstract void configureServer();

    protected LinkedBindingBuilder<MetricEventsListenerFactory> bindEventsListenerFactory() {
        return bind(metricEventsListenerFactoryKey);
    }

    protected ServerConfigBuilder server() {
        return serverConfigBuilder;
    }

    public static class ServerConfig {
        private final int port;
        private final int threadPoolSize;

        public ServerConfig(int port, int threadPoolSize) {
            this.port = port;
            this.threadPoolSize = threadPoolSize;
        }

        public int getPort() {
            return port;
        }

        public int getThreadPoolSize() {
            return threadPoolSize;
        }
    }

    public class ServerConfigBuilder {

        private int port = 8080;
        private int poolSize = Runtime.getRuntime().availableProcessors();

        public ServerConfigBuilder port(int port) {
            this.port = port;
            return this;
        }

        public ServerConfigBuilder threadPoolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public void bind() {
            ServerConfig config = new ServerConfig(port, poolSize);
            ExpServerModule.this.bind(serverConfigKey).toInstance(config);
        }
    }

}
