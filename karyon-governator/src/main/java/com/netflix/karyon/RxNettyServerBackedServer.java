package com.netflix.karyon;

import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import io.reactivex.netty.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link KaryonServer} which wraps an RxNetty's server.
 *
 * @author Nitesh Kant
 */
class RxNettyServerBackedServer extends MainClassBasedServer {

    private static final Logger logger = LoggerFactory.getLogger(RxNettyServerBackedServer.class);

    @SuppressWarnings("rawtypes") private final AbstractServer rxNettyServer;

    RxNettyServerBackedServer(AbstractServer rxNettyServer, LifecycleInjectorBuilderSuite... suites) {
        super(RxNettyServerBackedServer.class, suites);
        this.rxNettyServer = rxNettyServer;
    }

    @Override
    public void start() {
        super.start();
        rxNettyServer.start();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            rxNettyServer.shutdown();
        } catch (InterruptedException e) {
            logger.error("Interrupted while shutdown.", e);
            Thread.interrupted();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void waitTillShutdown() {
        try {
            rxNettyServer.waitTillShutdown();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for shutdown.", e);
            Thread.interrupted();
            throw new RuntimeException(e);
        }
    }

}
