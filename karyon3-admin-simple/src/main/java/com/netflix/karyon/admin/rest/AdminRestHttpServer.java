package com.netflix.karyon.admin.rest;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.netflix.karyon.admin.AdminServer;
import com.netflix.karyon.admin.HttpServerConfig;
import com.netflix.karyon.admin.SimpleHttpServer;
import com.sun.net.httpserver.HttpHandler;

@Singleton
public class AdminRestHttpServer extends SimpleHttpServer {
    @Inject
    public AdminRestHttpServer(
            @AdminServer HttpServerConfig config, 
            @AdminServer Map<String, HttpHandler> otherHandlers, 
            AdminHttpHandler adminHandler) throws IOException {
        super(config, ImmutableMap.<String, HttpHandler>builder().putAll(otherHandlers).put("/", adminHandler).build());
    }

}
