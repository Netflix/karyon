package com.netflix.karyon.ws.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import rx.Observable;

@Path("/v1/hello")
public class HelloWorldResource {
    @GET
    @Path("{name}")
    public Observable<String> getStatus(@PathParam("name") String name) {
        return Observable.just("hello " + name);
    }
}
