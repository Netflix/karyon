package com.netflix.karyon.rxnetty;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import javax.inject.Singleton;

import rx.Observable;

import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;
import com.netflix.governator.LifecycleShutdownSignal;

public final class ShutdownServerModule extends DefaultModule {

    // Here we create a 2nd RxNetty server on a different port.  
    // This example demonstrates how to create shutdown port.  
    // TBD: Should this be part of RxNettyModule
    @Provides
    @Singleton
    @RxNettyShutdown
    HttpServer getShutdownServer(@RxNettyShutdown ServerConfig config, final LifecycleShutdownSignal shutdown) {
        System.out.println("Shutdown Config : " + config);
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
    @RxNettyShutdown
    ServerConfig getShutdownConfig(ConfigProxyFactory factory) {
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
