package com.netflix.karyon.ws.rs.test;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import rx.Observable;

public interface BaseResource {

    @GET
    @Path("/str/{name}")
    public Observable<String> getString(@PathParam("name") String name);
    
    @GET
    @Path("/int/{name}")
    public Observable<String> getInteger(@PathParam("name") Integer name);

    @GET
    @Path("/double/{name}")
    public Observable<String> getDouble(@PathParam("name") Double name);

    @GET
    @Path("/long/{name}")
    public Observable<String> getLong(@PathParam("name") Long name);

    @GET
    @Path("/header")
    public Observable<String> getHeader(@DefaultValue("default") @HeaderParam("foo") String foo);
        
    @GET
    @Path("/two/{first}/{last}")
    public Observable<String> getTwoPathParam(
            @PathParam("first") String first, 
            @PathParam("last") String last);

    @GET
    @Path("/default")
    public Observable<String> getDefaultPathParam(
            @QueryParam("notdefined") @DefaultValue("default") String notDefined);

    @GET
    @Path("/list/int")
    public List<Integer> getListOfIntegers();

    @GET
    @Path("/single/int")
    public Integer getInteger();

    @GET
    @Path("/observable/list/int")
    public Observable<Integer> getObservableListOfIntegers();

    @GET
    @Path("/observable/single/int")
    public Observable<Integer> getObservableInteger();

}