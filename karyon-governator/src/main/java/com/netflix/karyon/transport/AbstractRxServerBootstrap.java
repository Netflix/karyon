package com.netflix.karyon.transport;

import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.ConnectionBasedServerBuilder;
import io.reactivex.netty.server.RxServer;
import io.reactivex.netty.server.ServerMetricsEvent;

/**
 * @author Tomasz Bak
 */
public abstract class AbstractRxServerBootstrap<I, O, B extends ConnectionBasedServerBuilder<I, O, B>,
        M extends ServerMetricsEvent<? extends Enum<?>>> implements KaryonServerBootstrap {

    private final B serverBuilder;
    private final MetricEventsListenerFactory<I, O, M> metricEventsListenerFactory;
    private RxServer<I, O> server; // To avoid GC

    protected AbstractRxServerBootstrap(LazyDelegateConnectionHandler<I, O> lazyConnectionHandler,
                                        ConnectionHandler<I, O> connectionHandler,
                                        MetricEventsListenerFactory<I, O, M> metricEventsListenerFactory,
                                        B serverBuilder) {
        this.metricEventsListenerFactory = metricEventsListenerFactory;
        this.serverBuilder = serverBuilder;
        lazyConnectionHandler.setHandler(connectionHandler);
    }

    @Override
    public void startServer() throws Exception {
        _start();
        server.start();
    }

    @Override
    public void shutdown() throws InterruptedException {
        server.shutdown();
    }

    @Override
    public void waitTillShutdown() throws InterruptedException {
        server.waitTillShutdown();
    }

    protected void _start() throws Exception {
        server = serverBuilder.build();
        server.subscribe(metricEventsListenerFactory.createListener(server));
    }
}
