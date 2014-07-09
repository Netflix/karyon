package com.netflix.karyon;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.karyon.transport.http.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nitesh Kant
 */
public class KaryonServer {

    private static final Logger logger = LoggerFactory.getLogger(KaryonServer.class);

    private final Class<?> mainClass;

    public KaryonServer(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public void startAndAwait() throws Exception {
        Injector injector = LifecycleInjector.bootstrap(mainClass);
        TypeLiteral<ServerBootstrap<ByteBuf, ByteBuf>> bootstrapTypeLiteral = new TypeLiteral<ServerBootstrap<ByteBuf, ByteBuf>>() {};
        ServerBootstrap<ByteBuf, ByteBuf> serverBootstrap = injector.getInstance(Key.get(bootstrapTypeLiteral));
        serverBootstrap.startServerAndWait();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: " + KaryonServer.class.getCanonicalName() + " <main classs name>");
            System.exit(-1);
        }

        String mainClassName = args[0];
        System.out.println("Using main class: " + mainClassName);

        KaryonServer server;
        try {
            server = new KaryonServer(Class.forName(mainClassName));
            server.startAndAwait();
        } catch (ClassNotFoundException e) {
            System.out.println("Main class: " + mainClassName + "not found.");
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Error while starting karyon server.", e);
        }
    }
}
