package netflix.karyon.transport.http.websockets;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.metrics.MetricEventsListenerFactory;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.protocol.http.websocket.WebSocketServerBuilder;
import io.reactivex.netty.server.RxServer;
import netflix.karyon.transport.AbstractServerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

import static netflix.karyon.utils.TypeUtils.keyFor;

/**
 * @author Tomasz Bak
 */
public class WebSocketsRxServerProvider<I extends WebSocketFrame, O extends WebSocketFrame, S extends RxServer<I, O>> implements Provider<S> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketsRxServerProvider.class);

    private final Named nameAnnotation;

    protected final Key<ConnectionHandler<I, O>> connectionHandlerKey;
    @SuppressWarnings("rawtypes")
    private final Key<PipelineConfigurator> pipelineConfiguratorKey;
    private final Key<MetricEventsListenerFactory> metricEventsListenerFactoryKey;
    private final Key<AbstractServerModule.ServerConfig> serverConfigKey;

    private RxServer<I, O> server;

    public WebSocketsRxServerProvider(String name, Class<I> iType, Class<O> oType) {
        nameAnnotation = Names.named(name);

        connectionHandlerKey = keyFor(ConnectionHandler.class, iType, oType, nameAnnotation);
        pipelineConfiguratorKey = Key.get(PipelineConfigurator.class, nameAnnotation);
        metricEventsListenerFactoryKey = Key.get(MetricEventsListenerFactory.class, nameAnnotation);
        serverConfigKey = Key.get(AbstractServerModule.ServerConfig.class, nameAnnotation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public S get() {
        return (S) server;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        if (server != null) {
            logger.info("Starting WebSockets server {} on port {}...", nameAnnotation.value(), server.getServerPort());
            server.shutdown();
        }
    }

    @Inject
    @SuppressWarnings("unchecked")
    public void setInjector(Injector injector) {
        KaryonWebSocketsModule.WebSocketsServerConfig config = (KaryonWebSocketsModule.WebSocketsServerConfig) injector.getInstance(serverConfigKey);

        ConnectionHandler<I, O> connectionHandler = injector.getInstance(connectionHandlerKey);

        WebSocketServerBuilder<I, O> builder = RxNetty.newWebSocketServerBuilder(config.getPort(), connectionHandler)
                .withMessageAggregator(config.isMessageAggregator());

        if (injector.getExistingBinding(pipelineConfiguratorKey) != null) {
            builder.appendPipelineConfigurator(injector.getInstance(pipelineConfiguratorKey));
        }

        if (injector.getExistingBinding(metricEventsListenerFactoryKey) != null) {
            builder.withMetricEventsListenerFactory(injector.getInstance(metricEventsListenerFactoryKey));
        }

        server = builder.build().start();
        logger.info("Starting WebSockets server {} on port {}...", nameAnnotation.value(), server.getServerPort());
    }
}
