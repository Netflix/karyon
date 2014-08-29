package com.netflix.karyon.ws.rs;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;

import javax.inject.Provider;
import javax.ws.rs.DELETE;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.netflix.karyon.ws.rs.RoutingRequestHandler.Builder;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * Configure a RoutingRequestHandler from a type
 * @author elandau
 *
 */
public class ClassRouterConfigurer implements RouterConfigurer {
    private static final Logger LOG = LoggerFactory.getLogger(ClassRouterConfigurer.class);
    
    private final Class<?> resource;
    private final InjectionSpi injector;
    
    public ClassRouterConfigurer(InjectionSpi injector, Class<?> resource) {
        this.resource = resource;
        this.injector = injector;
    }
    
    @Override
    public Builder configure(Builder builder) {
        Provider<?> provider = injector.getProvider(resource);
        Path rootPath = resource.getAnnotation(Path.class);
        LOG.info("Creating resource from '{}' with path '{}'", resource.getName(), rootPath.value());

        Preconditions.checkNotNull(rootPath, "Resource class must have a path");
        for (Method method : resource.getDeclaredMethods()) {
            Path path = method.getAnnotation(Path.class);
            String regex = (path == null) ? ".*" : path.value();

            if (!regex.startsWith("/")) {
                regex = "/" + regex;
            }
            HttpMethod verb = null;
            if (null != method.getAnnotation(POST.class)) {
                verb = HttpMethod.POST;
            }
            else if (null != method.getAnnotation(DELETE.class)) {
                verb = HttpMethod.DELETE;
            }
            else if (null != method.getAnnotation(PUT.class)) {
                verb = HttpMethod.PUT;
            }
            else if (null != method.getAnnotation(HEAD.class)) {
                verb = HttpMethod.HEAD;
            }
            else if (null != method.getAnnotation(OPTIONS.class)) {
                verb = HttpMethod.OPTIONS;
            }
            
            
            LOG.info("Creating route: template={} verb={} method={}", regex, verb, resource.getName() + ":" + method.getName());
            builder.withRoute(new Route(new UriTemplate(regex), verb, new MethodRequestHandler(provider, method)));
        }
        
        return builder;
    }

}
