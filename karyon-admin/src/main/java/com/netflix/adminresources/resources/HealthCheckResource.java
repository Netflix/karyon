package com.netflix.adminresources.resources;

import com.google.inject.Inject;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeoutException;

/**
 * A health check resource available via {@link com.netflix.adminresources.AdminResourcesContainer}, this in turn just
 * calls the {@link HealthCheckInvocationStrategy} to invoke the configured {@link com.netflix.karyon.spi.HealthCheckHandler}
 *
 * @author Nitesh Kant
 */
@Path("/healthcheck")
public class HealthCheckResource {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckResource.class);

    private HealthCheckInvocationStrategy healthCheckInvocationStrategy;

    @Inject
    public HealthCheckResource(HealthCheckInvocationStrategy healthCheckInvocationStrategy) {
        this.healthCheckInvocationStrategy = healthCheckInvocationStrategy;
    }

    @GET
    public Response checkHealth() {
        try {
            int status = healthCheckInvocationStrategy.invokeCheck();
            return Response.status(status).build();
        } catch (TimeoutException e) {
            logger.error("Karyon health check failed via adminresource health endpoint. Returning 500", e);
            return Response.serverError().build();
        }
    }
}
