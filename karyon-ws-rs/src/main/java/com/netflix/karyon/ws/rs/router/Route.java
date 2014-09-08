package com.netflix.karyon.ws.rs.router;

import java.util.Map;

import rx.Observable;

import com.netflix.karyon.ws.rs.RequestContext;

public interface Route {

    boolean match(RequestContext context, Map<String, String> groupValues);

    Observable<Void> call(RequestContext childContext);
    
}
