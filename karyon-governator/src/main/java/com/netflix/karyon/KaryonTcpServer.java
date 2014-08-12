package com.netflix.karyon;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.karyon.transport.tcp.TcpServerBootstrap;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomasz Bak
 */
public class KaryonTcpServer {
    private static final Logger logger = LoggerFactory.getLogger(KaryonServer.class);

    private final Class<?> mainClass;

    public KaryonTcpServer(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public void startAndAwait() throws Exception {
        Injector injector = LifecycleInjector.bootstrap(mainClass);
        TypeLiteral<TcpServerBootstrap<ByteBuf, ByteBuf>> bootstrapTypeLiteral = new TypeLiteral<TcpServerBootstrap<ByteBuf, ByteBuf>>() {
        };
        TcpServerBootstrap<ByteBuf, ByteBuf> serverBootstrap = injector.getInstance(Key.get(bootstrapTypeLiteral));
        serverBootstrap.startServerAndWait();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: " + KaryonServer.class.getCanonicalName() + " <main classs name>");
            System.exit(-1);
        }

        String mainClassName = args[0];
        System.out.println("Using main class: " + mainClassName);

        KaryonTcpServer server;
        try {
            server = new KaryonTcpServer(Class.forName(mainClassName));
            server.startAndAwait();
        } catch (ClassNotFoundException e) {
            System.out.println("Main class: " + mainClassName + "not found.");
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Error while starting karyon server.", e);
        }
    }

}
