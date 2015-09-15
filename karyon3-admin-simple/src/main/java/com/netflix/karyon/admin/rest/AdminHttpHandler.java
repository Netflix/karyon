package com.netflix.karyon.admin.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.archaius.Config;
import com.netflix.karyon.admin.AdminServer;
import com.netflix.karyon.admin.rest.exception.NotFoundException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@Singleton
public class AdminHttpHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AdminHttpHandler.class);

    private final Provider<ResourceContainer> resources;
    private final ObjectMapper mapper;
    private final AdminServerConfig config;

    private Config cfg;

    @Inject
    public AdminHttpHandler(
            @AdminServer ObjectMapper mapper,
            @AdminServer Provider<ResourceContainer> controllers, 
            AdminServerConfig config,
            Config cfg) {
        this.resources = controllers;
        this.mapper = mapper;
        this.config = config;
        this.cfg = cfg;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LOG.info("'{}'", exchange.getRequestURI());

        final String path = exchange.getRequestURI().getPath();
        
        exchange.getResponseHeaders().set("Server", "KaryonAdmin");
        
        try {
            // Redirect the server root to the configured remote server using the naming convension
            //  
            if (path.equals("/")) {
                String addr = new Interpolator(cfg).interpolate(config.remoteServer());
                exchange.getResponseHeaders().set("Location", addr);
                exchange.sendResponseHeaders(302, 0);
                exchange.close();
            }
            else if (path.equals("/favicon.ico")) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
            }
            else {
                String parts[] = path.substring(1).split("/");
                String controller = parts[0];
                List<String> p = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    p.add(parts[i]);
                }
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", config.accessControlAllowOrigin());
                
                Object response = resources.get().invoke(controller, p);
                if (response instanceof String) {
                    writeResponse(exchange, 200, (String)response);
                }
                else {
                    writeResponse(exchange, 200, mapper.writeValueAsString(response));
                }
            }
        }
        catch (NotFoundException e) {
            writeResponse(exchange, 404, e.getMessage());
        }
        catch (Exception e) {
            LOG.error("Error processing request '" + path + "'", e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            writeResponse(exchange, 500, sw.toString());
        }
    }

    private boolean shouldUseGzip(HttpExchange exchange) {
        final String encoding = exchange.getRequestHeaders().getFirst("Accept-Encoding");
        return (encoding != null && encoding.toLowerCase().contains("gzip"));
    }

    private OutputStream getOutputStream(boolean useGzip, HttpExchange exchange) throws IOException {
        return useGzip
            ? new GZIPOutputStream(exchange.getResponseBody())
            : exchange.getResponseBody();
    }

    private void writeResponse(HttpExchange exchange, int code, String content) throws IOException {
        final boolean useGzip = shouldUseGzip(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (useGzip) {
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        }
        exchange.sendResponseHeaders(code, content.length());
        try (OutputStream os = getOutputStream(useGzip, exchange)) {
            os.write(content.getBytes());
        }
    }
}
