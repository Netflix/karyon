package com.netflix.karyon.admin.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.netflix.archaius.Config;
import com.netflix.archaius.annotations.ConfigurationSource;
import com.netflix.karyon.admin.AdminService;
import com.netflix.karyon.admin.AdminServiceRegistry;
import com.netflix.karyon.admin.CachingStaticResourceProvider;
import com.netflix.karyon.admin.FileSystemResourceProvider;
import com.netflix.karyon.admin.StaticResource;
import com.netflix.karyon.admin.StaticResourceProvider;
import com.netflix.karyon.admin.rest.exception.NotFoundException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@Singleton
@ConfigurationSource("karyon_admin")
public class AdminHttpHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AdminHttpHandler.class);

    static class ServiceDefinition {
        ServiceDefinition(Map<String, MethodInvoker> invokers, MethodInvoker indexInvoker) {
            this.invokers = invokers;
            this.indexInvoker = indexInvoker;
        }
        
        final Map<String, MethodInvoker> invokers;
        final MethodInvoker indexInvoker;
    }
    
    static interface MethodInvoker {
        Object invoke(InputStream stream, Map<String, String> queryParameters) throws Exception;
    }
    
    static class OptionalArgs {
        @com.google.inject.Inject(optional=true)
        @AdminServerFallback
        HttpHandler fallbackHandler;
    }
    
    private final StaticResourceProvider        provider;
    private final ObjectMapper                  mapper;
    private final AdminServerConfig             config;
    private final HttpHandler                   fallback;
    private final Map<String, ServiceDefinition> services = new HashMap<>();

    private Config cfg;

    @Inject
    public AdminHttpHandler(
            AdminServiceRegistry services, 
            AdminServerConfig config,
            Config cfg, 
            OptionalArgs optional) {
        this.mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        if (config.prettyPrint()) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        this.config = config;
        this.cfg = cfg;
        
        FileSystemResourceProvider resourceProvider = new FileSystemResourceProvider(
                config.resourcePath(),
                config.mimeTypesResourceName());
        
        this.provider = config.cacheResources() ? new CachingStaticResourceProvider(resourceProvider) : resourceProvider;
        this.fallback = optional.fallbackHandler != null ? optional.fallbackHandler : new NotFoundHttpHandler();
        
        for (final String serviceName : services.getServiceNames()) {
            Map<String, MethodInvoker> invokers = new HashMap<>();
            
            final Class<?> serviceClass = services.getServiceClass(serviceName);
            AdminService annot = serviceClass.getAnnotation(AdminService.class);
            
            for (final Method method : serviceClass.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                
                if (invokers.containsKey(method.getName())) {
                    LOG.warn("Method '{}' already exists", method.toGenericString());
                    continue;
                }
                
                MethodInvoker invoker = null;
                if (method.getParameterCount() == 0) {
                    invoker = new MethodInvoker() {
                        @Override
                        public Object invoke(InputStream stream, Map<String, String> queryParameters) throws Exception {
                            // TODO: Parse stream and queryParameters into request object
                            if (stream.available() > 0 || !queryParameters.isEmpty()) {
                                throw new UnsupportedOperationException("Query parameters or request object not supported yet");
                            }
                            return method.invoke(services.getService(serviceName));
                        }
                    };
                }
                else if (method.getParameterCount() == 1) {
                    final Class<?> requestType = method.getParameterTypes()[0];
                    invoker = new MethodInvoker() {
                        @Override
                        public Object invoke(InputStream stream, Map<String, String> queryParameters) throws Exception {
                            // TODO: Parse stream and queryParameters into request object
                            if (stream.available() > 0 || !queryParameters.isEmpty()) {
                                throw new UnsupportedOperationException("Query parameters or request object not supported yet");
                            }
                            return method.invoke(services.getService(serviceName), mapper.readValue(stream, requestType));
                        }
                    };
                }
                else {
                    LOG.warn("Method '{}' can only have one argument", method.toGenericString());
                    continue;
                }
                
                invokers.put(method.getName(), invoker);
            }
            
            LOG.info("Found service : {} : {}", serviceName, invokers.keySet());
            
            this.services.put(serviceName, new ServiceDefinition(invokers, invokers.get(annot.index())));
        }
    }
    
    private Object invoke(String serviceName, String methodName, InputStream is, Map<String, String> queryParameters) throws NotFoundException, Exception {
        ServiceDefinition def = services.get(serviceName);
        if (def == null) {
            throw new NotFoundException("Service " + serviceName + " not found");
        }
        
        MethodInvoker invoker = methodName == null ? def.indexInvoker : def.invokers.get(methodName);
        if (invoker == null) {
            throw new NotFoundException("Method " + methodName + " for service " + serviceName + " not found");
        }
        return invoker.invoke(is, queryParameters);
    }
    
    private Map<String, String> extractQueryParameters(URI uri) {
        Map<String, String> query = new HashMap<String, String>();
        String queryString = uri.getQuery();
        if (queryString != null) {
            for (String param : queryString.split("&")) {
                String pair[] = param.split("=");
                if (pair.length > 1) {
                    query.put(pair[0], pair[1]);
                }
                else{
                    query.put(pair[0], "");
                }
            }
        }
        return query;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LOG.debug("'{}'", exchange.getRequestURI());

        exchange.getResponseHeaders().set("Server", "KaryonAdmin");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", config.accessControlAllowOrigin());
        
        final String path = exchange.getRequestURI().getPath();
        
        try {
            String parts[] = path.substring(1).split("/");
            // Redirect the server root to the configured remote server
            if (path.equals("/")) {
                String addr = new Interpolator(cfg).interpolate(config.remoteServer());
                LOG.debug("Redirecting to '{}'", addr);
                writeRedirectResponse(exchange, addr);
            }
            else {
                String serviceName;
                String methodName;
                
                if (parts.length == 1) {
                    serviceName = parts[0];
                    methodName = null;
                }
                else if (parts.length == 2) {
                    serviceName = parts[0];
                    methodName = parts[1];
                }
                else {
                    throw new NotFoundException("");
                }
                
                Map<String, String> query = extractQueryParameters(exchange.getRequestURI());

                Object response = invoke(serviceName, methodName, exchange.getRequestBody(), query);
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", config.accessControlAllowOrigin());
                writeJsonResponse(exchange, 200, response);
            }
        }
        // If no resource found then try to serve static content
        catch (NotFoundException e) {
            Optional<StaticResource> resource;
            try {
                resource = provider.getResource(path).get();
                if (resource.isPresent()) {
                    writeFileResponse(exchange, 200, resource.get().getData(), resource.get().getMimeType());
                }
                else {
                    fallback.handle(exchange);
                }
            } catch (Exception e1) {
                LOG.error("Error processing request '" + path + "'", e);
                writeErrorResponse(exchange, e);
            }
        }
        catch (Exception e) {
            LOG.error("Error processing request '" + path + "'", e);
            writeErrorResponse(exchange, e);
        }
        finally {
            exchange.close();
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
