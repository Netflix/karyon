package com.netflix.karyon.ws.rs.router;

import io.netty.handler.codec.http.HttpMethod;

import java.util.Map;

import rx.Observable;

import com.google.common.base.Preconditions;
import com.netflix.karyon.ws.rs.RequestContext;
import com.netflix.karyon.ws.rs.RequestHandler;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * Association between a UriTemplate and a RequestHandler.
 * 
 * @author elandau
 *
 */
public class UriTemplateRoute implements Route {
    public static interface Completion {
        void built(UriTemplateRoute route);
    }
    
    private static class NullCompletion implements Completion {
        @Override
        public void built(UriTemplateRoute route) {
        }
    }
    
    public static class Builder {
        private UriTemplate template;
        private HttpMethod verb = HttpMethod.GET;
        private RequestHandler handler;
        private Completion completion;
        
        Builder(Completion completion) {
            this.completion = completion;
        }
        
        public Builder uri(String path) {
            this.template = new UriTemplate(path);
            return this;
        }
        
        public Builder uri(UriTemplate template) {
            this.template = template;
            return this;
        }
        
        public Builder verb(HttpMethod verb) {
            this.verb = verb;
            return this;
        }
        
        public UriTemplateRoute through(RequestHandler handler) {
            Preconditions.checkNotNull(template);
            Preconditions.checkNotNull(handler);
            this.handler = handler;
            UriTemplateRoute route = new UriTemplateRoute(this);
            completion.built(route);
            return route;
        }

        UriTemplateRoute builder() {
            return new UriTemplateRoute(this);
        }
    }
    
    public static Builder builder() {
        return new Builder(new NullCompletion());
    }
    
    public static Builder builder(Completion completion) {
        return new Builder(completion);
    }
    
    public UriTemplateRoute(Builder builder) {
        this.handler = builder.handler;
        this.template = builder.template;
        this.verb = builder.verb;
    }
    
    private final UriTemplate template;
    private final RequestHandler handler;
    private final HttpMethod verb;
    
    @Override
    public final boolean match(RequestContext context, Map<String, String> vars) {
        if (verb != null && !context.getRequest().getHttpMethod().equals(verb)) {
            return false;
        }
        if (!template.match(context.getFragment(), vars)) {
            return false;
        }
        return true;
    }
    
    @Override
    public Observable<Void> call(RequestContext context) {
        return handler.call(context);
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
            .append("Route [")
            .append("template=").append(template)
            .append(", handler=").append(handler)
            .append(", verb=").append(verb)
            .append("]")
            .toString();
    }
}
