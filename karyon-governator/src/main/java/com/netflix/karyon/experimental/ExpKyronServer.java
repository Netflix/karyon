package com.netflix.karyon.experimental;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import io.reactivex.netty.server.RxServer;

/**
 * @author Tomasz Bak
 */
public class ExpKyronServer {

    private final RxServer<?, ?>[] servers;

    public ExpKyronServer(RxServer<?, ?>... servers) {
        this.servers = servers;
    }

    public void start() {
        for (RxServer<?, ?> server : servers) {
            server.start();
        }
    }

    public void waitTillShutdown() throws InterruptedException {
        for (RxServer<?, ?> server : servers) {
            server.waitTillShutdown();
        }
    }

    protected static void bootstrap(Class<? extends ExpKyronServer> serverBootstrapClass) {
        Injector injector = LifecycleInjector.bootstrap(serverBootstrapClass);
        LifecycleManager lifecycleManager = injector.getInstance(LifecycleManager.class);

        try {
            lifecycleManager.start();
            ExpKyronServer bootstrap = injector.getInstance(serverBootstrapClass);
            bootstrap.start();
            bootstrap.waitTillShutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
