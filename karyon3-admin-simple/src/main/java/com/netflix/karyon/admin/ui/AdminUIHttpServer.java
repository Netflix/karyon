package com.netflix.karyon.admin.ui;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.admin.AdminUIServer;
import com.netflix.karyon.admin.HttpServerConfig;
import com.netflix.karyon.admin.SimpleHttpServer;
import com.sun.net.httpserver.HttpHandler;

@Singleton
public class AdminUIHttpServer extends SimpleHttpServer {
    @Inject
    public AdminUIHttpServer(
            @AdminUIServer HttpServerConfig config, 
            AdminUIHttpHandler handler) throws IOException {
        super(config, Collections.<String, HttpHandler>singletonMap("/", handler));
    }
}
