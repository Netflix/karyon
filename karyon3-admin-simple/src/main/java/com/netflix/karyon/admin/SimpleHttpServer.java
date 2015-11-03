package com.netflix.karyon.admin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.karyon.http.simple.PathCountingHttpHandlerFilter;
import com.netflix.karyon.http.simple.FilteringHttpHandler;
import com.netflix.karyon.http.simple.HttpHandlerFilter;
import com.netflix.karyon.http.simple.LoggingHttpHandlerFilter;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SimpleHttpServer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpServer.class);
    
    private final ExecutorService  service;
    private final HttpServer       server;
    private final HttpServerConfig config;
    private final PathCountingHttpHandlerFilter counters;
    
    public SimpleHttpServer(HttpServerConfig config, Map<String, HttpHandler> handler) throws IOException {
        this(config, handler, new ArrayList<>());
    }
    
    public SimpleHttpServer(HttpServerConfig config, Map<String, HttpHandler> handler, List<HttpHandlerFilter> filters) throws IOException {
        ArrayList<HttpHandlerFilter> _filters = new ArrayList<>();
        _filters.add(counters = new PathCountingHttpHandlerFilter());
        _filters.add(new LoggingHttpHandlerFilter());
        _filters.addAll(filters);
        
        this.config = config;
        this.service = Executors.newFixedThreadPool(
                config.threads(), 
                new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat(config.name() + "-%d")
                    .build());
        
        InetSocketAddress inetSocketAddress = new InetSocketAddress(config.port());
        server = HttpServer.create(inetSocketAddress, config.backlog());
        server.setExecutor(service);
        
        for (Entry<String, HttpHandler> entry : handler.entrySet()) {
            server.createContext(entry.getKey(), new FilteringHttpHandler(_filters, entry.getValue()));
        }
        
        if (config.enabled()) {
            server.start();
        }
        
        LOG.info("Started server on port {}", config.port());
    }
    
    @PreDestroy 
    public void shutdown() {
        LOG.info("Shutting server on port {}", config.port());
        server.stop(config.shutdownDelay());
        service.shutdownNow();
    }

    public int getServerPort() {
        return config.port();
    }

    public Map<String, AtomicLong> getPathRequestCounts() {
        return counters.getCounts();
    }
    
    public InetSocketAddress getServerAddress() {
        return server.getAddress();
    }

}
