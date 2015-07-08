package com.netflix.karyon.example.rxnetty;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import javax.inject.Named;
import javax.inject.Singleton;

import rx.Observable;

import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.governator.DefaultModule;
import com.netflix.governator.LifecycleShutdownSignal;
import com.netflix.karyon.rxnetty.ServerConfig;

public class ShutdownServerModule extends DefaultModule {

    // Here we create a 2nd RxNetty server on a different port.  
    // This example demonstrates how to create shutdown port.  
    // TBD: Should this be part of RxNettyModule
    @Provides
    @Singleton
    @Named("shutdown")
    HttpServer getShutdownServer(@Named("shutdown") ServerConfig config, final LifecycleShutdownSignal shutdown) {
        System.out.println("Shutdown Config : " + config);
        return HttpServer
            .newServer(config.getServerPort())
            .start(new RequestHandler<ByteBuf, ByteBuf>() {
                @Override
                public Observable<Void> handle(
                        HttpServerRequest<ByteBuf> request,
                        HttpServerResponse<ByteBuf> response) {
                    shutdown.signal();
                    return response.writeString(Observable.just("Shutting down"));
                }
            });
    }
    
    // This binds our main server to Archaius configuration using the prefix
    // 'karyon.rxnetty.shutdown'. See helloworld.properties
    @Provides
    @Singleton
    @Named("shutdown")
    ServerConfig getShutdownConfig(ConfigProxyFactory factory) {
        return factory.newProxy(ServerConfig.class, "karyon.rxnetty.shutdown");
    }

}
