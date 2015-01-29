package netflix.karyon.transport.tcp;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.metrics.MetricEventsListenerFactory;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.server.RxServer;
import io.reactivex.netty.server.ServerBuilder;
import netflix.karyon.transport.AbstractServerModule.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

import static netflix.karyon.utils.TypeUtils.keyFor;

/**
 * @author Tomasz Bak
 */
@SuppressWarnings("unchecked")
public class TcpRxServerProvider<I, O, S extends RxServer<I, O>> implements Provider<S> {

    private static final Logger logger = LoggerFactory.getLogger(TcpRxServerProvider.class);

    private final Named nameAnnotation;

    protected final Key<ConnectionHandler<I, O>> connectionHandlerKey;
    @SuppressWarnings("rawtypes")
    private final Key<PipelineConfigurator> pipelineConfiguratorKey;
    private final Key<MetricEventsListenerFactory> metricEventsListenerFactoryKey;
    private final Key<ServerConfig> serverConfigKey;

    private RxServer<I, O> server;

    public TcpRxServerProvider(String name, Class<I> iType, Class<O> oType) {
        nameAnnotation = Names.named(name);

        connectionHandlerKey = keyFor(ConnectionHandler.class, iType, oType, nameAnnotation);
        pipelineConfiguratorKey = Key.get(PipelineConfigurator.class, nameAnnotation);
        metricEventsListenerFactoryKey = Key.get(MetricEventsListenerFactory.class, nameAnnotation);
        serverConfigKey = Key.get(ServerConfig.class, nameAnnotation);
    }

    @Override
    public S get() {
        return (S) server;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (server != null) {
            server.shutdown();
        }
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

        server = builder.build().start();
        logger.info("Starting server {} on port {}...", nameAnnotation.value(), server.getServerPort());
    }
}
