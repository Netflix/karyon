package com.netflix.karyon.example.rxnetty;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.guice.ArchaiusModule;
import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.governator.LifecycleShutdownSignal;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.rxnetty.RxNettyModule;
import com.netflix.karyon.rxnetty.ServerConfig;

@Singleton
public class RxNettyHelloWorldApp extends DefaultLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(RxNettyHelloWorldApp.class);
    
    public static void main(String[] args) throws InterruptedException {
        new Karyon()
            .addModules(
                 new RxNettyModule(),   // Needed to start the RxNetty servers
                 new AbstractModule() {
                    @Override
                    protected void configure() {
                        // Just so we can log startup and shutdown
                        bind(RxNettyHelloWorldApp.class).asEagerSingleton();
                    }
                    
                    // This is our main RxNetty server
                    @Provides
                    @Named("main")
                    HttpServer getServer(ServerConfig config) {
                        final AtomicInteger counter = new AtomicInteger();
                        
                        return HttpServer
                            .newServer(config.getServerPort())
                            .start(new RequestHandler<ByteBuf, ByteBuf>() {
                                @Override
                                public Observable<Void> handle(
                                        HttpServerRequest<ByteBuf> request,
                                        HttpServerResponse<ByteBuf> response) {
                                    return response.writeString(Observable.just("Hello World " + counter.incrementAndGet() + "!"));
                                }
                            });
                    }
                    
                    // This binds our main server to Archaius configuration using the default
                    // prefix 'karyon.rxnetty' on ServerConfig.  See helloworld.properties
                    @Provides
                    @Singleton
                    ServerConfig getConfig(ConfigProxyFactory factory) {
                        return factory.newProxy(ServerConfig.class);
                    }
                    
                    // Here we create a 2nd RxNetty server on a different port.  
                    // This example demonstrates how to create shutdown port.  
                    // TBD: Should this be part of RxNettyModule
                    @Provides
                    @Named("shutdown")
                    HttpServer getShutdownServer(@Named("shutdown") ServerConfig config, final LifecycleShutdownSignal shutdown) {
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
            )
            .addBootstrapModule(new ArchaiusModule())
            .withConfigName("helloworld")
            .createInjector()
            .awaitTermination();
    }
    
    @Override
    public void onStarted() {
        LOG.info("Application Started");
    }
    
    @Override
    public void onStopped() {
        LOG.info("Application Stopped");
    }
}
