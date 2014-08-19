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
import com.netflix.karyon.transport.http.GovernatorHttpInterceptorSupport;
import com.netflix.karyon.transport.http.HttpRequestHandler;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.metrics.MetricEventsListenerFactory;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;

/**
 * @author Tomasz Bak
 */
public class HttpRxServerProvider<I, O, S extends HttpServer<I, O>> implements ProviderWithDependencies<S> {

    private final Named nameAnnotation;

    private final Key<HttpRequestRouter> routerKey;
    private final Key<PipelineConfigurator> pipelineConfiguratorKey;
    private final Key<MetricEventsListenerFactory> metricEventsListenerFactoryKey;
    private final Key<ServerConfig> serverConfigKey;

    private final Set<Dependency<?>> deps;
    private volatile HttpServer<I, O> httpServer;

    public HttpRxServerProvider(String name) {
        nameAnnotation = Names.named(name);

        routerKey = Key.get(HttpRequestRouter.class, nameAnnotation);
        pipelineConfiguratorKey = Key.get(PipelineConfigurator.class, nameAnnotation);
        metricEventsListenerFactoryKey = Key.get(MetricEventsListenerFactory.class, nameAnnotation);
        serverConfigKey = Key.get(ServerConfig.class, nameAnnotation);

        Set<Dependency<?>> deps = new HashSet<Dependency<?>>();
        Collections.addAll(deps,
                Dependency.get(routerKey),
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
        return (S) httpServer;
    }

    @Inject
    public void setInjector(Injector injector) {
        ServerConfig config = injector.getInstance(serverConfigKey);

        HttpRequestRouter router = injector.getInstance(routerKey);

        GovernatorHttpInterceptorSupport<I, O> interceptorSupport = new GovernatorHttpInterceptorSupport<I, O>();
        HttpRequestHandler<I, O> httpRequestHandler = new HttpRequestHandler<I, O>(router, interceptorSupport);

        HttpServerBuilder<I, O> builder = RxNetty.newHttpServerBuilder(config.getPort(), httpRequestHandler)
                .withRequestProcessingThreads(config.getThreadPoolSize());

        if (injector.getExistingBinding(pipelineConfiguratorKey) != null) {
            builder.appendPipelineConfigurator(injector.getInstance(pipelineConfiguratorKey));
        }

        if (injector.getExistingBinding(metricEventsListenerFactoryKey) != null) {
            builder.withMetricEventsListenerFactory(injector.getInstance(metricEventsListenerFactoryKey));
        }

        httpServer = builder.build();
    }
}
