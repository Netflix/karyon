package com.netflix.karyon.ws.rs;

import java.util.Set;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * RouterConfigurer for the top level RequestHandler passed to WsRsRequestHandler
 * 
 * @author elandau
 *
 */
public class PackagesRouterConfigurer implements RouterConfigurer {
    private static final Logger LOG = LoggerFactory.getLogger(PackagesRouterConfigurer.class);
    
    private final Set<String> pkgs;

    private final InjectionSpi injector;
    
    public static class Builder {
        private Set<String> pkgs = Sets.newHashSet();
        private InjectionSpi injector;
        
        public Builder withPackages(String pkgs) {
            if (pkgs != null) {
                this.pkgs.addAll(Sets.newHashSet(Splitter.on(";").split(pkgs)));
            }
            return this;
        }
        
        public Builder withInjectionSpi(InjectionSpi injector) {
            this.injector = injector;
            return this;
        }
        
        public PackagesRouterConfigurer build() {
            return new PackagesRouterConfigurer(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public PackagesRouterConfigurer(Builder builder) {
        this.pkgs = ImmutableSet.copyOf(builder.pkgs);
        this.injector = builder.injector;
    }

    @Override
    public com.netflix.karyon.ws.rs.RoutingRequestHandler.Builder configure(com.netflix.karyon.ws.rs.RoutingRequestHandler.Builder builder) {
        for (String pkg : pkgs) {
            LOG.info("Searching resources : " + pkg);
            try {
                for (ClassInfo cls : ClassPath.from(Thread.currentThread().getContextClassLoader()).getTopLevelClassesRecursive(pkg)) {
                    Class<?> type = Class.forName(cls.getName());
                    Path path = type.getAnnotation(Path.class);
                    if (path != null) {
                        builder.withRoute(
                            new Route(
                                new UriTemplate(path.value() + "{_: .*}"), 
                                null, 
                                new ClassRouterConfigurer(injector, type).configure(RoutingRequestHandler.builder()).build()));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure routes", e);
            }
        }
        return builder;
    }
}
