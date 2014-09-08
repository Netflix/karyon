package com.netflix.karyon.ws.rs.router;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Map;

import rx.Observable;

import com.netflix.karyon.ws.rs.RequestContext;

public class NotFoundRoute implements Route {
    @Override
    public boolean match(RequestContext context,Map<String, String> groupValues) {
        return true;
    }

    @Override
    public Observable<Void> call(RequestContext context) {
        context.getResponse().setStatus(HttpResponseStatus.NOT_FOUND);
        return Observable.empty();
    }

    @Override
    public String toString() {
        return "NotFoundRoute []";
    }

}
