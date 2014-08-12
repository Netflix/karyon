package com.netflix.karyon.transport;

import com.google.inject.Injector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.ShutdownListener;
import io.reactivex.netty.server.ConnectionBasedServerBuilder;
import io.reactivex.netty.server.RxServer;
import io.reactivex.netty.server.ServerMetricsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action0;

/**
 * @author Tomasz Bak
 */
public abstract class AbstractRxServerBootstrap<I, O, B extends ConnectionBasedServerBuilder<I, O, B>,
        M extends ServerMetricsEvent<? extends Enum<?>>> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRxServerBootstrap.class);

    private final int shutdownPort;
    private final B serverBuilder;
    private final MetricEventsListenerFactory<I, O, M> metricEventsListenerFactory;
    private ShutdownListener shutdownListener; // To avoid GC
    private RxServer<I, O> server; // To avoid GC
    private final LifecycleManager lifecycleManager;

    protected AbstractRxServerBootstrap(Ports ports, Injector injector,
                                        MetricEventsListenerFactory<I, O, M> metricEventsListenerFactory,
                                        B serverBuilder) {
        shutdownPort = ports.getShutdownPort();
        this.metricEventsListenerFactory = metricEventsListenerFactory;
        this.serverBuilder = serverBuilder;
        lifecycleManager = injector.getInstance(LifecycleManager.class);
    }

    public void startServer() throws Exception {
        _start();
        server.startAndWait();
    }

    public void startServerAndWait() throws Exception {
        startServer();
        server.waitTillShutdown();
    }

    protected void _start() throws Exception {
        server = serverBuilder.build();
        server.subscribe(metricEventsListenerFactory.createListener(server));

        shutdownListener = new ShutdownListener(shutdownPort, new Action0() {
            @Override
            public void call() {
                try {
                    server.shutdown();
                } catch (InterruptedException e) {
                    logger.error("Failed to shutdown server.", e);
                }
                lifecycleManager.close();
            }
        });
        shutdownListener.start();
        lifecycleManager.start();
    }

}
