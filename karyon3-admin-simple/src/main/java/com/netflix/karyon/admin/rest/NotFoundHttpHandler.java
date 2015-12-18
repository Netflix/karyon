package com.netflix.karyon.admin.rest;

import java.io.IOException;
import java.io.OutputStream;

import com.google.common.base.Charsets;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class NotFoundHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(404, 0);
        try (OutputStream out = exchange.getResponseBody()) {
            String msg = "Resource '" + exchange.getRequestURI().getPath() + "' not found.\n";
            out.write(msg.getBytes(Charsets.UTF_8));
        }
    }
}