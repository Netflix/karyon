package com.netflix.karyon.ws.rs.router;

import io.netty.handler.codec.http.HttpHeaders;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Func1;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.netflix.karyon.ws.rs.RequestContext;
import com.netflix.karyon.ws.rs.RequestHandler;
import com.netflix.karyon.ws.rs.binders.StringBinder;
import com.netflix.karyon.ws.rs.binders.StringBinderFactory;
import com.netflix.karyon.ws.rs.providers.ResponseWriter;
import com.netflix.karyon.ws.rs.providers.ResponseWriterFactory;

/**
 * RequestHandler for a Resource method
 * 
 * @author elandau
 *
 */
public class MethodRequestHandler implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MethodRequestHandler.class);
    
    private static class ParameterInfo {
        DefaultValue defaultValue = null;

        public String getDefaultValue() {
            if (defaultValue == null)
                return null;
            return defaultValue.value();
        }
    }
    
    public static interface Resolver extends Func1<RequestContext, Object> {
    }
    
    public static interface Invoker {
        Observable<?> invoke(Object[] object);
    }
    
    private final Provider<?> resourceProvider; 
    private final Method method;
    private final List<Resolver> resolvers = Lists.newArrayList();
    private final ResponseWriterFactory responseWriterFactory;
    private final List<MediaType> produces;
    private final List<MediaType> consumes;
    private final Invoker invoker;
    
    public MethodRequestHandler(
            final Method method, 
            String[] produces,
            String[] consumes,
            final Provider<?> resourceProvider, 
            StringBinderFactory stringBinderFactory,
            ResponseWriterFactory responseWriterFactory) {
        this.resourceProvider = resourceProvider;
        this.method = method;
        this.responseWriterFactory = responseWriterFactory;
        
        Annotation[][] parameters = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            final Class<?> type = method.getParameterTypes()[i];
            final ParameterInfo info = new ParameterInfo();
            Resolver resolver = null;
            for (Annotation annot : parameters[i]) {
                if (annot.annotationType().equals(PathParam.class)) {
                    Preconditions.checkArgument(resolver == null, "Multiple value binders not allowed");
                    final StringBinder<?> binder = stringBinderFactory.create(type);
                    final PathParam pathParam = (PathParam)annot;
                    resolver = new Resolver() {
                        @Override
                        public Object call(RequestContext context) {
                            return binder.bind(context.getVars().get(pathParam.value()));
                        }
                    };
                }
                else if (annot.annotationType().equals(QueryParam.class)) {
                    Preconditions.checkArgument(resolver == null, "Multiple value binders not allowed");
                    final QueryParam queryParam = (QueryParam)annot;
                    if (List.class.isAssignableFrom(type)) {
                        final StringBinder<?> binder = stringBinderFactory.create(type);
                        resolver = new Resolver() {
                            @Override
                            public Object call(RequestContext context) {
                                return context.getRequest().getQueryParameters().get(queryParam.value());
                            }
                        };
                    }
                    else {
                        final StringBinder<?> binder = stringBinderFactory.create(type);
                        resolver = new Resolver() {
                            @Override
                            public Object call(RequestContext context) {
                                List<String> values = context.getRequest().getQueryParameters().get(queryParam.value());
                                if (values == null) {
                                    String defaultValue = info.getDefaultValue();
                                    if (defaultValue != null) {
                                        return binder.bind(defaultValue);
                                    }
                                    return null;
                                }
                                Preconditions.checkArgument(values.size() == 1, "Multiple strings not allowed");
                                return binder.bind(values.get(0));
                            }
                        };
                    }
                }
                else if (annot.annotationType().equals(HeaderParam.class)) {
                    Preconditions.checkArgument(resolver == null, "Multiple value binders not allowed");
                    final HeaderParam headerParam = (HeaderParam)annot;
                    if (List.class.isAssignableFrom(type)) {
                        final StringBinder<?> binder = stringBinderFactory.create(type);
                        resolver = new Resolver() {
                            @Override
                            public Object call(RequestContext context) {
                                return context.getRequest().getHeaders().get(headerParam.value());
                            }
                        };
                    }
                    else {
                        final StringBinder<?> binder = stringBinderFactory.create(type);
                        resolver = new Resolver() {
                            @Override
                            public Object call(RequestContext context) {
                                List<String> values = context.getRequest().getHeaders().getAll(headerParam.value());
                                if (values == null || values.isEmpty()) {
                                    String defaultValue = info.getDefaultValue();
                                    if (defaultValue != null) {
                                        return binder.bind(defaultValue);
                                    }
                                    return null;
                                }
                                Preconditions.checkArgument(values.size() == 1, "Multiple strings not allowed");
                                return binder.bind(values.get(0));
                            }
                        };
                    }
                }
                else if (annot.annotationType().equals(MatrixParam.class)) {
                    Preconditions.checkArgument(resolver == null, "Multiple value binders not allowed");
                    final MatrixParam matrixParam = (MatrixParam)annot;
                }
                else if (annot.annotationType().equals(DefaultValue.class)) {
                    Preconditions.checkArgument(info.defaultValue == null, "Multiple default values not allowed");
                    info.defaultValue = (DefaultValue)annot;
                }
                else {
                    LOG.warn("Unknown parameter annotation : " + annot.annotationType().getName());
                }
            }
            
            if (resolver != null) {
                resolvers.add(resolver);
            }
            else if (HttpServerRequest.class.isAssignableFrom(type)) {
                resolvers.add(new Resolver() {
                    @Override
                    public Object call(RequestContext context) {
                        return context.getRequest();
                    }
                });
            }
            else if (HttpServerResponse.class.isAssignableFrom(type)) {
                resolvers.add(new Resolver() {
                    @Override
                    public Object call(RequestContext context) {
                        return context.getResponse();
                    }
                });
            }
            else if (HttpHeaders.class.isAssignableFrom(type)) {
                resolvers.add(new Resolver() {
                    @Override
                    public Object call(RequestContext context) {
                        return context.getRequest().getHeaders();
                    }
                });
            }
            else if (javax.ws.rs.core.HttpHeaders.class.isAssignableFrom(type)) {
                resolvers.add(new Resolver() {
                    @Override
                    public Object call(RequestContext context) {
                        return null;
                    }
                });
            }
            else {
                // Placeholder that resolves to null
                resolvers.add(new Resolver() {
                    @Override
                    public Object call(RequestContext context) {
                        return null;
                    }
                });
            }
        }
        
        Produces methodProduces = method.getAnnotation(Produces.class);
        Consumes methodConsumes = method.getAnnotation(Consumes.class);
        
        this.produces = Lists.newArrayList();
        for (String p : methodProduces != null ? methodProduces.value() : produces) {
            this.produces.add(MediaType.valueOf(p));
        }
        this.consumes = Lists.newArrayList();
        for (String c : methodConsumes != null ? methodConsumes.value() : consumes) {
            this.consumes.add(MediaType.valueOf(c));
        }
        
        if (Observable.class.isAssignableFrom(method.getReturnType())) {
            this.invoker = new Invoker() {
                @Override
                public Observable<Object> invoke(Object[] params) {
                    try {
                        return (Observable) method.invoke(resourceProvider.get(), params);
                    }
                    catch (Exception e) {
                        return Observable.error(e);
                    }
                }
            };
        }
        else {
            this.invoker = new Invoker() {
                @Override
                public Observable<Object> invoke(Object[] params) {
                    try {
                        return Observable.just(method.invoke(resourceProvider.get(), params));
                    }
                    catch (Exception e) {
                        return Observable.error(e);
                    }
                }
            };
        }
    }
    
    @Override
    public Observable<Void> call(final RequestContext context) {
        LOG.info("Processing request : " + context.getFragment());
        context.getVars().remove("_");
        
        String accept = context.getRequest().getHeaders().getHeader(HttpHeaders.Names.ACCEPT);
        if (accept == null) {
            accept = MediaType.WILDCARD_TYPE.toString();
        }
        for (String type : Splitter.on(",").split(accept)) {
            MediaType acceptType = MediaType.valueOf(type);
            for (MediaType produceType : produces) {
                if (acceptType.isCompatible(produceType)) {
                    ResponseWriter<?> writer = responseWriterFactory.getResponseWriter(
                            method.getReturnType(), method.getGenericReturnType(), method.getAnnotations(), produceType); 
                    
                    if (writer == null) {
                        writer = responseWriterFactory.getResponseWriter(
                                method.getReturnType(), method.getGenericReturnType(), method.getAnnotations(), acceptType); 
                    }
                    
                    if (writer != null) {
                        Object[] params = new Object[resolvers.size()];
                        int i = 0;
                        for (Resolver resolver : resolvers) {
                            params[i++] = resolver.call(context);
                        }
    
                        try {
                            return writer.write(
                                    invoker.invoke(params),
                                    method.getReturnType(),
                                    method.getGenericReturnType(),
                                    method.getAnnotations(),
                                    produceType,
                                    context.getResponse());
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                }
            }
        }
        return Observable.error(new RuntimeException("Handler not found"));
    }

    public String toString() {
        return new StringBuilder()
            .append("MethodHandler [")
            .append(method.getDeclaringClass().getName()).append(":").append(method.getName())
            .append(", consumes:").append(Joiner.on(",").join(consumes))
            .append(", produces:").append(Joiner.on(",").join(produces))
            .append("]")
            .toString();

    }


}
