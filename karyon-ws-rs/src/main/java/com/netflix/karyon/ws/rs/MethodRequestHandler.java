package com.netflix.karyon.ws.rs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Provider;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Func1;

import com.google.common.collect.Lists;

/**
 * RequestHandler for a Resource method
 * 
 * @author elandau
 *
 */
public class MethodRequestHandler implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MethodRequestHandler.class);
    
    private final Provider<?> resourceProvider; 
    private final Method method;
    
    public static interface Resolver extends Func1<RequestContext, Object> {
    }
    
    private final List<Resolver> resolvers = Lists.newArrayList();
    
    public MethodRequestHandler(Provider<?> resourceProvider, Method method) {
        this.resourceProvider = resourceProvider;
        this.method = method;
        
        Annotation[][] parameters = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            for (Annotation annot : parameters[i]) {
                if (annot.annotationType().equals(PathParam.class)) {
                    final PathParam param = (PathParam)annot;
                    resolvers.add(new Resolver() {
                        @Override
                        public Object call(RequestContext context) {
                            return context.getVars().get(param.value());
                        }
                    });
                }
            }
        }
    }

    @Override
    public Observable<Object> call(RequestContext context) {
        context.getVars().remove("_");
        
        Object[] params = new Object[resolvers.size()];
        int i = 0;
        for (Resolver resolver : resolvers) {
            params[i++] = resolver.call(context);
        }
        try {
            return (Observable<Object>) method.invoke(resourceProvider.get(), params);
        } catch (Exception e) {
            return Observable.error(e);
        }
    }

}
