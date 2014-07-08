package com.netflix.karyon;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.karyon.transport.http.ServerBootstrap;
import io.netty.buffer.ByteBuf;

/**
 * @author Nitesh Kant
 */
public class KaryonServer {

    private final Class<?> mainClass;

    public KaryonServer(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public void startAndAwait() {
        Injector injector = LifecycleInjector.bootstrap(mainClass);
        TypeLiteral<ServerBootstrap<ByteBuf, ByteBuf>> bootstrapTypeLiteral = new TypeLiteral<ServerBootstrap<ByteBuf, ByteBuf>>() {};
        ServerBootstrap<ByteBuf, ByteBuf> serverBootstrap = injector.getInstance(Key.get(bootstrapTypeLiteral));
        serverBootstrap.startServerAndWait();
    }

    public static void main(String[] args) throws ClassNotFoundException {
        String mainClassName = args[0];
        System.out.println("Using main class: " + mainClassName);
        KaryonServer server = new KaryonServer(Class.forName(mainClassName));
        server.startAndAwait();
    }
}
