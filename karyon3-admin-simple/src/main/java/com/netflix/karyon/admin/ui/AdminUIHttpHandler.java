package com.netflix.karyon.admin.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.karyon.admin.CachingStaticResourceProvider;
import com.netflix.karyon.admin.FileSystemResourceProvider;
import com.netflix.karyon.admin.StaticResource;
import com.netflix.karyon.admin.StaticResourceProvider;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@Singleton
public class AdminUIHttpHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AdminUIHttpHandler.class);

    private final AdminUIServerConfig config;
    private final StaticResourceProvider provider;
    
    @Inject
    public AdminUIHttpHandler(AdminUIServerConfig config) {
        this.config = config;
        this.provider = 
            new CachingStaticResourceProvider(
                new FileSystemResourceProvider(
                    config.resourcePath(),
                    config.mimeTypesResourceName()));
    }
    
    @Override
    public void handle(HttpExchange arg0) throws IOException {
        String path = arg0.getRequestURI().getPath();
        LOG.debug("'{}'", path);
        
        Optional<StaticResource> resource;
        try {
            resource = provider.getResource(path).get();
            if (resource.isPresent()) {
                writeResponse(arg0, 200, resource.get().getData(), resource.get().getMimeType());
                return;
            }
            else {
                LOG.debug("'{}' Not Found", path);
                writeResponse(arg0, 404, "not found".getBytes(), null);
            }
        } 
        catch (Exception e) {
            LOG.debug("'{}' Not Found", path);
            writeResponse(arg0, 404, "not found".getBytes(), null);
        }
        finally {
            arg0.close();
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
    private void writeResponse(HttpExchange arg0, int code, byte[] content, String mimeType) throws IOException {
        arg0.getResponseHeaders().set("Server:", config.getServerName());
        
        if (mimeType != null) {
            arg0.getResponseHeaders().set("Content-Type", mimeType);
        }
        arg0.sendResponseHeaders(code, content.length);
        
        OutputStream os = arg0.getResponseBody();
        os.write(content);
    }
}
