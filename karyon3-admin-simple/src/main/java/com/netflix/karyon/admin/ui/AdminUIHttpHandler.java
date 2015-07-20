package com.netflix.karyon.admin.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.netflix.karyon.admin.rest.AdminHttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@Singleton
public class AdminUIHttpHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AdminHttpHandler.class);

    private final ConcurrentMap<String, String> templates = new ConcurrentHashMap<>();
    private final AdminUIServerConfig config;
    private final MimetypesFileTypeMap mimeTypes;
    
    @Inject
    public AdminUIHttpHandler(AdminUIServerConfig config) {
        this.config = config;
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream(config.mimeTypesResourceName());
            if (is != null) {
                mimeTypes  = new MimetypesFileTypeMap(is);
            }
            else {
                throw new RuntimeException("Unable to load mime.types file");
            }
        }
        finally { 
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.debug("Unable to close mime type file");
                }
            }
        }
    }
    
    @Override
    public void handle(HttpExchange arg0) throws IOException {
        String path = arg0.getRequestURI().getPath();
        LOG.info("'{}'", path);
        
        try {
            String resource = String.format("/%s%s", config.resourcePath(), path);
            String content = getResource(resource);
            if (content != null) {
                String mimeType  = mimeTypes.getContentType(resource);
                writeResponse(arg0, 200, content, mimeType);
                return;
            }
            
            LOG.info("'{}' Not Fund", path);
            writeResponse(arg0, 404, "not found", null);
            return;
        }
        catch (Exception e) {
            LOG.error("'{}' Internal error", path, e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            writeResponse(arg0, 500, sw.toString(), null);
            return;
        }
    }
    
    /**
     * Send a response
     * 
     * @param arg0
     * @param code
     * @param content
     * @param mimeType
     * @throws IOException
     */
    private void writeResponse(HttpExchange arg0, int code, String content, String mimeType) throws IOException {
        arg0.getResponseHeaders().set("Server:", config.getServerName());
        
        if (mimeType != null) {
            arg0.getResponseHeaders().set("Content-Type", mimeType);
        }
        arg0.sendResponseHeaders(code, content.length());
        
        OutputStream os = arg0.getResponseBody();
        os.write(content.getBytes());
        os.close();
    }
    
    /**
     * Load a resource from the class path with optional caching of loaded resources
     * 
     * @param name
     * @return
     * @throws IOException
     */
    private String getResource(String name) throws IOException {
        String template = templates.get(name);
        if (template != null) {
            return template;
        }
        
        try (final InputStream is = this.getClass().getResourceAsStream(name)) {
            if (is != null) {
                try (final Reader reader = new InputStreamReader(is)) {
                    template = CharStreams.toString(reader);
                    if (config.cacheResources()) {
                        templates.putIfAbsent(name, template);
                    }
                    return template;
                }
            }
            else {
                return null;
            }
        }
    }
}
