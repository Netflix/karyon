package com.netflix.adminresources.resources;

import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Nitesh Kant
 */
@Path("/admin/env")
@Beta
@Produces(MediaType.APPLICATION_JSON)
public class EnvironmentResource {

    @GET
    public Response getAllProperties() {
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String propsJson = gson.toJson(System.getenv());
        return Response.ok(propsJson).build();
    }
}
