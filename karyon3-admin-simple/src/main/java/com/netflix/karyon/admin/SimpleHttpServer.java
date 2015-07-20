package com.netflix.karyon.admin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SimpleHttpServer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpServer.class);
    
    private final ExecutorService  service;
    private final HttpServer       server;
    private final HttpServerConfig config;
    
    public SimpleHttpServer(HttpServerConfig config, Map<String, HttpHandler> handler) throws IOException {
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
        
        for (Entry<String, HttpHandler> entry : handler.entrySet()) {
            server.createContext(entry.getKey(), entry.getValue());
        }
        
        if (config.enabled()) {
            server.start();
        }
        
        LOG.info("Started admin server on port {}", config.port());
    }
    
    @PreDestroy 
    public void shutdown() {
        server.stop(config.shutdownDelay());
    }

    public int getServerPort() {
        return config.port();
    }

    public InetSocketAddress getServerAddress() {
        return server.getAddress();
    }

}
