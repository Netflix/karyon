package com.netflix.karyon.rxnetty.shutdown;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import javax.inject.Singleton;

import rx.Observable;

import com.google.inject.Provides;
import com.netflix.archaius.Config;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.visitor.PrintStreamVisitor;
import com.netflix.governator.DefaultModule;
import com.netflix.governator.LifecycleShutdownSignal;
import com.netflix.karyon.http.ServerConfig;

public final class ShutdownServerModule extends DefaultModule {

    // Here we create a 2nd RxNetty server on a different port.  
    // This example demonstrates how to create shutdown port.  
    // TBD: Should this be part of RxNettyModule
    @Provides
    @Singleton
    @ShutdownServer
    HttpServer<ByteBuf, ByteBuf> getShutdownServer(@ShutdownServer ServerConfig config, final LifecycleShutdownSignal shutdown) {
        return RxNetty.newHttpServerBuilder(
            config.getServerPort(), 
            new RequestHandler<ByteBuf, ByteBuf>() {
                @Override
                public Observable<Void> handle(
                        HttpServerRequest<ByteBuf> request,
                        HttpServerResponse<ByteBuf> response) {
                    shutdown.signal();
                    return response.writeStringAndFlush("Shutting down");
                }
            })
            .build();
    }
    
    // This binds our main server to Archaius configuration using the prefix
    // 'karyon.rxnetty.shutdown'. See helloworld.properties
    @Provides
    @Singleton
    @ShutdownServer
    ServerConfig getShutdownConfig(Config config, ConfigProxyFactory factory) {
        config.accept(new PrintStreamVisitor());
        return factory.newProxy(ShutdownServerConfig.class);
    }

    @Override
    public boolean equals(Object obj) {
        return ShutdownServerModule.class.equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return ShutdownServerModule.class.hashCode();
    }
}
