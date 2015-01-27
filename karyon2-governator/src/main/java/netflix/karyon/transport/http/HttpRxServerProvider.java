package netflix.karyon.transport.http;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.reactivex.netty.metrics.MetricEventsListenerFactory;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.AbstractServerModule.ServerConfig;
import netflix.karyon.transport.KaryonTransport;
import netflix.karyon.transport.http.KaryonHttpModule.HttpServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

import static netflix.karyon.utils.TypeUtils.keyFor;

/**
 * @author Tomasz Bak
 */
@SuppressWarnings("unchecked")
public class HttpRxServerProvider<I, O, S extends HttpServer<I, O>> implements Provider<S> {

    private static final Logger logger = LoggerFactory.getLogger(HttpRxServerProvider.class);

    private final Named nameAnnotation;

    private final Key<RequestHandler<I, O>> routerKey;
    private final Key<GovernatorHttpInterceptorSupport<I, O>> interceptorSupportKey;
    @SuppressWarnings("rawtypes")
    private final Key<PipelineConfigurator> pipelineConfiguratorKey;
    private final Key<MetricEventsListenerFactory> metricEventsListenerFactoryKey;
    private final Key<ServerConfig> serverConfigKey;

    private volatile HttpServer<I, O> httpServer;

    public HttpRxServerProvider(String name, Class<I> iType, Class<O> oType) {
        nameAnnotation = Names.named(name);

        routerKey = keyFor(RequestHandler.class, iType, oType, nameAnnotation);
        interceptorSupportKey = keyFor(GovernatorHttpInterceptorSupport.class, iType, oType, nameAnnotation);
        pipelineConfiguratorKey = Key.get(PipelineConfigurator.class, nameAnnotation);
        metricEventsListenerFactoryKey = Key.get(MetricEventsListenerFactory.class, nameAnnotation);
        serverConfigKey = Key.get(ServerConfig.class, nameAnnotation);
    }

    @Override
    public S get() {
        return (S) httpServer;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (httpServer != null) {
            httpServer.shutdown();
        }
    }

    @SuppressWarnings("rawtypes")
    @Inject
    public void setInjector(Injector injector) {
        HttpServerConfig config = (HttpServerConfig) injector.getInstance(serverConfigKey);

        RequestHandler router = injector.getInstance(routerKey);

        GovernatorHttpInterceptorSupport<I, O> interceptorSupport = injector.getInstance(interceptorSupportKey);
        interceptorSupport.finish(injector);
        HttpRequestHandler<I, O> httpRequestHandler = new HttpRequestHandler<I, O>(router, interceptorSupport);

        HttpServerBuilder<I, O> builder = KaryonTransport.newHttpServerBuilder(config.getPort(), httpRequestHandler);

        if (config.requiresThreadPool()) {
            builder.withRequestProcessingThreads(config.getThreadPoolSize());
        }

        if (injector.getExistingBinding(pipelineConfiguratorKey) != null) {
            builder.appendPipelineConfigurator(injector.getInstance(pipelineConfiguratorKey));
        }

        if (injector.getExistingBinding(metricEventsListenerFactoryKey) != null) {
            builder.withMetricEventsListenerFactory(injector.getInstance(metricEventsListenerFactoryKey));
        }

        httpServer = builder.build().start();
        logger.info("Starting server {} on port {}...", nameAnnotation.value(), httpServer.getServerPort());
    }
}
