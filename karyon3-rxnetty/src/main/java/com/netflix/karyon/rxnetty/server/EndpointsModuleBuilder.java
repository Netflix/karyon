package com.netflix.karyon.rxnetty.server;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.UniqueAnnotations;
import com.google.inject.multibindings.Multibinder;
import com.netflix.karyon.http.UriPatternType;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.RequestHandler;

public class EndpointsModuleBuilder {

    private final Set<String> endpointUris = Sets.newHashSet();
    private final Binder binder;

    public EndpointsModuleBuilder(Binder binder) {
        this.binder = binder;
    }

    // the first level of the EDSL--
    public RxNettyServerModule.EndpointKeyBindingBuilder serve(Class<? extends Annotation> qualifier, List<String> urlPatterns) {
        return new EndpointKeyBindingBuilderImpl(qualifier, urlPatterns, UriPatternType.SERVLET);
    }

    public RxNettyServerModule.EndpointKeyBindingBuilder serveRegex(Class<? extends Annotation> qualifier, List<String> regexes) {
        return new EndpointKeyBindingBuilderImpl(qualifier, regexes, UriPatternType.REGEX);
    }

    // non-static inner class so it can access state of enclosing module class
    class EndpointKeyBindingBuilderImpl implements RxNettyServerModule.EndpointKeyBindingBuilder {
        private final List<String> uriPatterns;
        private final UriPatternType uriPatternType;
        private final Class<? extends Annotation> qualifier;

        private EndpointKeyBindingBuilderImpl(Class<? extends Annotation> qualifier, List<String> uriPatterns, UriPatternType uriPatternType) {
            this.uriPatterns = uriPatterns;
            this.uriPatternType = uriPatternType;
            this.qualifier = qualifier;
        }

        public void with(Class<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey) {
            with(Key.get(endpointKey));
        }

        public void with(Key<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey) {
            with(endpointKey, null);
        }

        public void with(RequestHandler<ByteBuf, ByteBuf> endpoint) {
            Key<RequestHandler<ByteBuf, ByteBuf>> endpointKey = Key.get(new TypeLiteral<RequestHandler<ByteBuf, ByteBuf>>() {},
                    UniqueAnnotations.create());
            binder.bind(endpointKey).toInstance(endpoint);
            with(endpointKey, endpoint);
        }

        private void with(Key<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey,
                RequestHandler<ByteBuf, ByteBuf> endpointInstance) {
            for (String pattern : uriPatterns) {
                // Ensure two endpoints aren't bound to the same pattern.
                if (!endpointUris.add(pattern)) {
                    binder.addError("More than one endpoint was mapped to the same URI pattern: " + pattern);
                } 
                else {
                    HttpEndpointDefinition def = new HttpEndpointDefinition(pattern, endpointKey,
                            UriPatternType.get(uriPatternType, pattern),
                            endpointInstance);
                    binder.requestInjection(def);
                    
                    Multibinder.newSetBinder(binder, HttpEndpointDefinition.class, qualifier).addBinding().toInstance(def);
                }
            }
        }
    }
}
