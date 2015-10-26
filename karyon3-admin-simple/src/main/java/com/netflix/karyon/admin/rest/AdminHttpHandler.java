package com.netflix.karyon.admin.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.archaius.Config;
import com.netflix.archaius.annotations.ConfigurationSource;
import com.netflix.karyon.admin.AdminServer;
import com.netflix.karyon.admin.CachingStaticResourceProvider;
import com.netflix.karyon.admin.FileSystemResourceProvider;
import com.netflix.karyon.admin.StaticResource;
import com.netflix.karyon.admin.StaticResourceProvider;
import com.netflix.karyon.admin.rest.exception.NotFoundException;
import com.netflix.karyon.admin.ui.AdminUIServerConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@Singleton
@ConfigurationSource("karyon_admin")
public class AdminHttpHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AdminHttpHandler.class);

    private final StaticResourceProvider        provider;
    private final Provider<ResourceContainer>   resources;
    private final ObjectMapper                  mapper;
    private final AdminServerConfig             config;
    private final HttpHandler                   fallback;

    private Config cfg;

    private static class OptionalArgs {
        @com.google.inject.Inject(optional=true)
        @AdminServerFallback
        HttpHandler fallbackHandler;
    }
    
    @Inject
    public AdminHttpHandler(
            @AdminServer ObjectMapper mapper,
            @AdminServer Provider<ResourceContainer> controllers, 
            AdminServerConfig config,
            AdminUIServerConfig uiConfig,
            Config cfg, 
            OptionalArgs optional) {
        this.resources = controllers;
        this.mapper = mapper;
        this.config = config;
        this.cfg = cfg;
        this.provider = 
                new CachingStaticResourceProvider(
                    new FileSystemResourceProvider(
                        uiConfig.resourcePath(),
                        uiConfig.mimeTypesResourceName()));
        this.fallback = optional.fallbackHandler != null ? optional.fallbackHandler : new NotFoundHttpHandler();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LOG.debug("'{}'", exchange.getRequestURI());

        exchange.getResponseHeaders().set("Server", "KaryonAdmin");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", config.accessControlAllowOrigin());
        
        final String path = exchange.getRequestURI().getPath();
        
        try {
            try {
                // Redirect the server root to the configured remote server
                if (path.equals("/")) {
                    String addr = new Interpolator(cfg).interpolate(config.remoteServer());
                    LOG.debug("Redirecting to '{}'", addr);
                    writeRedirectResponse(exchange, addr);
                }
                // Try to serve a resource
                else {
                    String parts[] = path.substring(1).split("/");
                    String controller = parts[0];
                    List<String> p = new ArrayList<>();
                    for (int i = 1; i < parts.length; i++) {
                        p.add(parts[i]);
                    }
                    
                    Object response = resources.get().invoke(controller, p);
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", config.accessControlAllowOrigin());
                    writeJsonResponse(exchange, 200, response);
                }
            }
            // If no resource found then try to serve static content
            catch (NotFoundException e) {
                Optional<StaticResource> resource = provider.getResource(path).get();
                if (resource.isPresent()) {
                    writeFileResponse(exchange, 200, resource.get().getData(), resource.get().getMimeType());
                }
                else {
                    fallback.handle(exchange);
                }
            }
        }
        catch (Exception e) {
            LOG.error("Error processing request '" + path + "'", e);
            writeErrorResponse(exchange, e);
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
    
    /**
     * Redirect the response to a different location
     */
    private void writeRedirectResponse(HttpExchange exchange, String addr) throws IOException {
        exchange.getResponseHeaders().set("Location", addr);
        exchange.sendResponseHeaders(302, 0);
        exchange.close();
    }
    
    /**
     * Write a JSON response
     */
    private void writeJsonResponse(HttpExchange exchange, int code, Object payload) throws IOException {
        
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", config.accessControlAllowOrigin());
        final boolean useGzip = shouldUseGzip(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        if (useGzip) {
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        }
        // Size of 0 indicates to use a chunked response
        exchange.sendResponseHeaders(code, 0);
        try (OutputStream os = getOutputStream(useGzip, exchange)) {
            if (payload instanceof String) {
                os.write(((String) payload).getBytes(Charset.forName("UTF-8")));
            } else {
                mapper.writeValue(os, payload);
            }
        }
    }
    
    /**
     * Write a static File response
     */
    private void writeFileResponse(HttpExchange exchange, int code, byte[] content, String mimeType) throws IOException {
        if (mimeType != null) {
            exchange.getResponseHeaders().set("Content-Type", mimeType);
        }
        exchange.sendResponseHeaders(code, content.length);
        
        OutputStream os = exchange.getResponseBody();
        os.write(content);
        os.close();
    }
    
    /**
     * Write an error response containing the stack trace.
     */
    private void writeErrorResponse(HttpExchange exchange, Exception e) throws IOException {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        writeJsonResponse(exchange, 500, sw.toString());
    }
}
