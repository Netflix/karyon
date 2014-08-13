package com.netflix.karyon;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.transport.KaryonServerBootstrap;
import com.netflix.karyon.transport.ServerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action0;

/**
 * @author Nitesh Kant
 */
public class KaryonServer {
    private static final Logger logger = LoggerFactory.getLogger(KaryonServer.class);

    private final Class<?> mainClass;
    private final List<KaryonServerBootstrap> bootstrapServers = new ArrayList<KaryonServerBootstrap>();
    private Injector injector;
    private ShutdownListener shutdownListener;
    private LifecycleManager lifecycleManager;

    public KaryonServer(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public void start() throws Exception {
        injector = LifecycleInjector.bootstrap(mainClass);
        startSubmodules();
        startShutdownListener();
        startLifecycleManager();
    }

    public void shutdown() {
        for (KaryonServerBootstrap serverBootstrap : bootstrapServers) {
            try {
                serverBootstrap.shutdown();
            } catch (InterruptedException e) {
                logger.error("Server shutdown interrupted", e);
            }
        }
        if (lifecycleManager != null) {
            lifecycleManager.close();
        }
    }

    public void waitTillShutdown() {
        for (KaryonServerBootstrap serverBootstrap : bootstrapServers) {
            try {
                serverBootstrap.waitTillShutdown();
            } catch (InterruptedException e) {
                // FIXME Shall we retry for the same server, or shutdown all?
                logger.warn("Server monitor thread interrupted", e);
            }
        }
    }

    private void startSubmodules() throws Exception {
        Submodules annotation = mainClass.getAnnotation(Submodules.class);
        if (annotation == null || annotation.include() == null || annotation.include().length == 0) {
            throw new IllegalArgumentException("ERROR: no submodules found on class " + mainClass.getName());
        }
        Class<? extends Module>[] submodules = annotation.include();

        for (Class<? extends Module> subModule : submodules) {
            Injector childInjector = injector.createChildInjector(subModule.newInstance());
            bootstrapFrom(childInjector);
        }
    }

    private void startShutdownListener() {
        ServerPort serverPort = injector.getInstance(Key.get(ServerPort.class, Names.named("shutdown")));

        shutdownListener = new ShutdownListener(serverPort.getPort(), new Action0() {
            @Override
            public void call() {
                shutdown();
            }
        });
        shutdownListener.start();
    }

    private void startLifecycleManager() throws Exception {
        lifecycleManager = injector.getInstance(LifecycleManager.class);
        lifecycleManager.start();
    }

    private void bootstrapFrom(Injector injector) throws Exception {
        for (Key<?> key : injector.getAllBindings().keySet()) {
            if (KaryonServerBootstrap.class.isAssignableFrom(key.getTypeLiteral().getRawType())) {
                KaryonServerBootstrap serverBootstrap = (KaryonServerBootstrap) injector.getInstance(key);
                bootstrapServers.add(serverBootstrap);
                serverBootstrap.startServer();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: " + KaryonServer.class.getCanonicalName() + " <main classs name>");
            System.exit(-1);
        }

        String mainClassName = args[0];
        System.out.println("Using main class: " + mainClassName);

        KaryonServer server = null;
        try {
            server = new KaryonServer(Class.forName(mainClassName));
            server.start();
            server.waitTillShutdown();
        } catch (@SuppressWarnings("UnusedCatchParameter") ClassNotFoundException e) {
            System.out.println("Main class: " + mainClassName + "not found.");
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Error while starting karyon server.", e);
            if (server != null) {
                server.shutdown();
            }
            System.exit(-1);
        }
        // In case we have non-daemon threads running
        System.exit(0);
    }
}
