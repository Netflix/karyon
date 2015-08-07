package com.netflix.karyon.admin.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
    public void handle(HttpExchange arg0) throws IOException {
        LOG.info("'{}'", arg0.getRequestURI());
        
        String path = arg0.getRequestURI().getPath();
        
        arg0.getResponseHeaders().set("Server", "KaryonAdmin");
        
        try {
            // Redirect the server root to the configured remote server using the naming convension
            //  
            if (path.equals("/")) {
                String addr = new Interpolator(cfg).interpolate(config.remoteServer());
                arg0.getResponseHeaders().set("Location", addr);
                arg0.sendResponseHeaders(302, 0);
                arg0.close();
            }
            else if (path.equals("/favicon.ico")) {
                arg0.sendResponseHeaders(404, 0);
                arg0.close();
            }
            else {
                String parts[] = path.substring(1).split("/");
                String controller = parts[0];
                List<String> p = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    p.add(parts[i]);
                }
                arg0.getResponseHeaders().set("Access-Control-Allow-Origin", config.accessControlAllowOrigin());
                
                Object response = resources.get().invoke(controller, p);
                if (response instanceof String) {
                    writeResponse(arg0, 200, (String)response);
                }
                else {
                    writeResponse(arg0, 200, mapper.writeValueAsString(response));
                }
            }
        }
        catch (NotFoundException e) {
            writeResponse(arg0, 404, e.getMessage());
        }
        catch (Exception e) {
            LOG.error("Error processing request '" + path + "'", e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            writeResponse(arg0, 500, sw.toString());
        }
    }
    
    private void writeResponse(HttpExchange arg0, int code, String content) throws IOException {
        arg0.getResponseHeaders().set("Content-Type",                "application/json");
        arg0.sendResponseHeaders(code, content.length());
        
        OutputStream os = arg0.getResponseBody();
        os.write(content.getBytes());
        os.close();
    }
}
