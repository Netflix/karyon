package com.netflix.karyon.ws.rs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Func1;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * URI routing node that calls a single Route based on the incoming request
 * 
 * @author elandau
 *
 */
public class RoutingRequestHandler implements RequestHandler {
    private static final Logger LOG = LoggerFactory.getLogger(RoutingRequestHandler.class);
    
    public static class Builder {
        private final List<Route> routes = Lists.newArrayList();
        
        public Builder withRoute(Route route) {
            routes.add(route);
            return this;
        }
        
        public Builder withRoutes(Collection<Route> route) {
            routes.addAll(routes);
            return this;
        }
        
        public RoutingRequestHandler build() {
            return new RoutingRequestHandler(this);
        }
    }

    private final ImmutableList<Route> routes;
    
    public static Builder builder() {
        return new Builder();
    }

    public RoutingRequestHandler(Builder builder) {
        routes = ImmutableList.copyOf(builder.routes);
    }

    @Override
    public Observable<Object> call(final RequestContext context) {
        LOG.info("Processing {} {} ", context.getRequest().getHttpMethod(), context.getFragment());
        final Map<String, String> vars = Maps.newHashMap();
        return Observable
            .from(routes)
            .flatMap(new Func1<Route, Observable<Route>>() {
                @Override
                public Observable<Route> call(Route route) {
                    if (route.getVerb() != null && !context.getRequest().getHttpMethod().equals(route.getVerb())) {
                        return Observable.empty();
                    }
                    if (!route.template.match(context.getFragment(), vars)) {
                        return Observable.empty();
                    }
                    return Observable.just(route);
                }
            })
            .doOnNext(RxUtil.info("Found a route:"))
            .flatMap(new Func1<Route, Observable<Object>>() {
                @Override
                public Observable<Object> call(Route route) {
                    MatchResult result = route.getTemplate().getPattern().match(context.getFragment());
                    RequestContext childContext = context.getChild(vars, vars.get("_"));
                    return route.getHandler().call(childContext);
                }
            });
    }
}
