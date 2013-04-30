package com.netflix.adminresources.resources;

import com.google.inject.Inject;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeoutException;

/**
 * @author Nitesh Kant
 */
@Path("/healthcheck")
@Singleton
public class HealthcheckResource {

    @Inject(optional = true)
    private HealthCheckInvocationStrategy invocationStrategy;

    @GET
    public Response doHealthCheck() {
        if (null != invocationStrategy) {
            try {
                int status = invocationStrategy.invokeCheck();
                return Response.status(status).build();
            } catch (TimeoutException e) {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
