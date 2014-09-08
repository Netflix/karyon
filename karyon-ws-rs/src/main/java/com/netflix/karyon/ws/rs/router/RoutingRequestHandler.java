package com.netflix.karyon.ws.rs.router;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Func1;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.karyon.ws.rs.RequestContext;
import com.netflix.karyon.ws.rs.RequestHandler;
import com.netflix.karyon.ws.rs.router.UriTemplateRoute.Completion;
import com.netflix.karyon.ws.rs.rx.RxUtil;

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
        
        public UriTemplateRoute.Builder route(String template) {
            return UriTemplateRoute.builder(new Completion() {
                @Override
                public void built(UriTemplateRoute route) {
                    LOG.info("Adding route : " + route);
                    routes.add(route);
                }
            }).uri(template);
        }
        
        public Builder withNotFoundHandler() {
            routes.add(new NotFoundRoute());
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
    public Observable<Void> call(final RequestContext context) {
        LOG.info("Processing {} {} ", context.getRequest().getHttpMethod(), context.getFragment());
        final Map<String, String> vars = Maps.newHashMap();
        return Observable
            .from(routes)
            .doOnNext(RxUtil.info("Trying route"))
            .filter(new Func1<Route, Boolean>() {
                @Override
                public Boolean call(Route route) {
                    return route.match(context, vars);
                }
            })
            .take(1)
            .doOnNext(RxUtil.info("Found a route:"))
            .flatMap(new Func1<Route, Observable<Void>>() {
                @Override
                public Observable<Void> call(Route route) {
                    RequestContext childContext = context.getChild(vars, vars.get("_"));
                    return route.call(childContext);
                }
            })
            .doOnError(RxUtil.error("Error"));
    }
}
