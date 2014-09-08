package com.netflix.karyon.ws.rs.router;

import java.util.Set;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Action1;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.netflix.karyon.ws.rs.IoCProviderFactory;
import com.netflix.karyon.ws.rs.binders.DefaultStringBinderFactory;
import com.netflix.karyon.ws.rs.binders.StringBinderFactory;
import com.netflix.karyon.ws.rs.rx.RxReflection;
import com.netflix.karyon.ws.rs.rx.RxUtil;
import com.netflix.karyon.ws.rs.writers.DefaultResponseWriterFactory;
import com.netflix.karyon.ws.rs.writers.ResponseWriterFactory;

/**
 * RouterConfigurer for the top level RequestHandler passed to WsRsRequestHandler
 * 
 * @author elandau
 *
 */
public class PackagesRouterConfigurer implements RouterConfigurer {
    private static final Logger LOG = LoggerFactory.getLogger(PackagesRouterConfigurer.class);
    
    public static class Builder {
        private Set<String> pkgs = Sets.newHashSet();
        private IoCProviderFactory injector;
        private StringBinderFactory stringBinderFactory = new DefaultStringBinderFactory();
        private ResponseWriterFactory responseWriterFactory = new DefaultResponseWriterFactory();
        
        public Builder withPackages(String pkgs) {
            if (pkgs != null) {
                this.pkgs.addAll(Sets.newHashSet(Splitter.on(";").split(pkgs)));
            }
            return this;
        }
        
        public Builder withIoCProviderFactory(IoCProviderFactory iocProviderFactory) {
            this.injector = iocProviderFactory;
            return this;
        }
        
        public PackagesRouterConfigurer build() {
            return new PackagesRouterConfigurer(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private final Set<String> pkgs;
    private final IoCProviderFactory iocProviderFactory;
    private final StringBinderFactory stringBinderFactory;
    private final ResponseWriterFactory responseWriterFactory;
    

    public PackagesRouterConfigurer(Builder builder) {
        this.pkgs = ImmutableSet.copyOf(builder.pkgs);
        this.iocProviderFactory = builder.injector;
        this.stringBinderFactory = builder.stringBinderFactory;
        this.responseWriterFactory = builder.responseWriterFactory;
    }

    @Override
    public com.netflix.karyon.ws.rs.router.RoutingRequestHandler.Builder configure(final com.netflix.karyon.ws.rs.router.RoutingRequestHandler.Builder builder) {
        Observable.from(pkgs)
            .doOnNext(RxUtil.info("Scanning package:"))
            .flatMap(RxReflection.scanPackageClasses())
            .flatMap(RxReflection.nameToClass())
            .filter(RxReflection.isConcrete())
            .doOnNext(RxUtil.info("Found class:"))
            .doOnError(RxUtil.error("Failed to scan packages"))
            .subscribe(new Action1<Class<?>>() {
                @Override
                public void call(final Class<?> type) {
                    Observable.just(type)
                        .flatMap(RxReflection.getAllSubclasses())
                        .filter(RxReflection.hasClassAnnotation(Path.class))
                        .take(1)
                        .subscribe(new Action1<Class<?>>() {
                            @Override
                            public void call(Class<?> def) {
                                Path path = def.getAnnotation(Path.class);
                                builder.route(path.value() + "{_: .*}")
                                       .through(new ClassRouterConfigurer(type, iocProviderFactory.getProvider(type), stringBinderFactory, responseWriterFactory)
                                           .configure(RoutingRequestHandler.builder()).build());
                            }
                        });
                    }
                }
            );
        builder.withNotFoundHandler();
        return builder;
    }
}
