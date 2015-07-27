package com.netflix.karyon.rxnetty;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import rx.Observable;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.netflix.karyon.http.UriPatternMatcher;

public class HttpEndpointDefinition {
    private final String pattern;
    private final Key<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey;
    private final UriPatternMatcher patternMatcher;
    private final AtomicReference<RequestHandler<ByteBuf, ByteBuf>> httpEndpoint = new AtomicReference<RequestHandler<ByteBuf, ByteBuf>>();

    public HttpEndpointDefinition(
            String pattern,
            Key<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey,
            UriPatternMatcher patternMatcher,
            RequestHandler<ByteBuf, ByteBuf> endpointInstance) {
        this.pattern = pattern;
        this.endpointKey = endpointKey;
        this.patternMatcher = patternMatcher;
        this.httpEndpoint.set(endpointInstance);
    }

    boolean shouldServe(String uri) {
        return uri != null && patternMatcher.matches(uri);
    }

    @Inject
    public void init(Injector injector) throws Exception {

        // This absolutely must be a singleton, and so is only initialized once.
        if (!Scopes.isSingleton(injector.getBinding(endpointKey))) {
            throw new Exception("Endpoints must be bound as singletons. "
                    + endpointKey + " was not bound in singleton scope.");
        }

        if (this.httpEndpoint.get() == null) {
            this.httpEndpoint.set(injector.getInstance(endpointKey));
        }
    }

    String getKey() {
        return endpointKey.toString();
    }

    String getPattern() {
        return pattern;
    }

    public Observable<Void> serve(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        // TODO: Modify path to remove context
        return httpEndpoint.get().handle(request, response);
    }
}