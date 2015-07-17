package com.netflix.karyon.admin.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@Singleton
public class AdminHttpServer {
    private final ExecutorService service;
    private final HttpServer server;
    private final AdminServerConfig config;
    
    @Inject
    public AdminHttpServer(@SimpleAdmin AdminServerConfig config, @SimpleAdmin HttpHandler handler) throws IOException {
        this.config = config;
        this.service = Executors.newFixedThreadPool(
                config.threads(), 
                new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat(config.name() + "-%d")
                    .build());
        
        InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", config.port());
        server = HttpServer.create(inetSocketAddress, config.backlog());
        server.setExecutor(service);
        server.createContext("/", handler);
        
        server.start();
    }
    
    @PreDestroy 
    public void shutdown() {
        server.stop(config.shutdownDelay());
    }

}
