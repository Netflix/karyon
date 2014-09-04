package com.netflix.karyon.ws.rs.router;

import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;

import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import com.netflix.karyon.ws.rs.binders.StringBinderFactory;
import com.netflix.karyon.ws.rs.providers.ResponseWriterFactory;
import com.netflix.karyon.ws.rs.router.RoutingRequestHandler.Builder;
import com.netflix.karyon.ws.rs.rx.RxReflection;

/**
 * Configure a RoutingRequestHandler from a type
 * @author elandau
 *
 */
public class ClassRouterConfigurer implements RouterConfigurer {
    private static final Logger LOG = LoggerFactory.getLogger(ClassRouterConfigurer.class);
    
    private final Class<?> resource;
    private final StringBinderFactory stringBinderFactory;
    private final ResponseWriterFactory responseWriterFactory;
    private final Provider<?> provider;
    
    public ClassRouterConfigurer(
            Class<?> resource, 
            Provider<?> resourceProvider,
            StringBinderFactory stringBinderFactory, 
            ResponseWriterFactory responseWriterFactory) {
        this.resource = resource;
        this.stringBinderFactory = stringBinderFactory;
        this.responseWriterFactory = responseWriterFactory;
        this.provider = resourceProvider;
    }
    
    @Override
    public Builder configure(final Builder builder) {
        Observable.just(resource)
            .flatMap(RxReflection.getAllSubclasses())
            .flatMap(RxReflection.getClassAnnotation(Path.class))
            .take(1)
            .cast(Path.class)
            .subscribe(new Action1<Path>() {
                @Override
                public void call(Path rootPath) {
                    LOG.info("Creating class from '{}' with path '{}'", resource.getName(), rootPath.value());
                    
                    final String[] classConsumes = Observable.just(resource)
                        .flatMap(RxReflection.getAllSubclasses())
                        .flatMap(RxReflection.getClassAnnotation(Consumes.class))
                        .take(1)
                        .cast(Consumes.class)
                        .map(new Func1<Consumes, String[]>() {
                            @Override
                            public String[] call(Consumes t1) {
                                return t1.value();
                            }
                        })
                        .toBlocking()
                        .firstOrDefault(new String[]{"*/*"});

                
                    final String[] classProduces = Observable.just(resource)
                        .flatMap(RxReflection.getAllSubclasses())
                        .flatMap(RxReflection.getClassAnnotation(Produces.class))
                        .take(1)
                        .cast(Produces.class)
                        .map(new Func1<Produces, String[]>() {
                            @Override
                            public String[] call(Produces t1) {
                                return t1.value();
                            }
                        })
                        .toBlocking()
                        .firstOrDefault(new String[]{"*/*"});
                
                    Observable.just(resource)
                        .flatMap(RxReflection.getAllSubclasses())
                        .flatMap(RxReflection.getDeclaredMethods())
                        .subscribe(new Action1<Method>() {
                            @Override
                            public void call(Method method) {
                                Path path = method.getAnnotation(Path.class);
                                String regex = (path == null) ? ".*" : path.value();
                    
                                if (!regex.startsWith("/")) {
                                    regex = "/" + regex;
                                }
                                HttpMethod verb = null;
                                if (null != method.getAnnotation(GET.class)) {
                                    verb = HttpMethod.GET;
                                }
                                else if (null != method.getAnnotation(POST.class)) {
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
                                
                                if (verb == null) {
                                    return;
                                }
                                
                                Produces produces = method.getAnnotation(Produces.class);
                                Consumes consumes = method.getAnnotation(Consumes.class);
                                
                                builder.route(regex).verb(verb).through(
                                        new MethodRequestHandler(
                                                method, 
                                                produces != null ? produces.value() : classProduces,
                                                consumes != null ? consumes.value() : classConsumes,
                                                provider, 
                                                stringBinderFactory, 
                                                responseWriterFactory));
                            }
                        });
                }
            });
        
        return builder;
    }
}
