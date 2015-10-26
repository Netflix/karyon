package com.netflix.karyon.admin.rest;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class NotFoundHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(404, 0);
        exchange.getResponseBody().write("not found".getBytes(Charsets.UTF_8));
    }
}