package netflix.adminresources;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import netflix.karyon.health.HealthCheckInvocationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeoutException;

@Path("/healthcheck")
@Produces(MediaType.TEXT_HTML)
@Singleton
public class HealthCheckResource {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckResource.class);
    private HealthCheckInvocationStrategy invocationStrategy;

    @Inject
    public HealthCheckResource(HealthCheckInvocationStrategy invocationStrategy) {
        this.invocationStrategy = invocationStrategy;
    }

    @GET
    public Response getHealthCheck() {
        try {
            final int status = invocationStrategy.invokeCheck();
            return Response.ok().status(status).build();
        } catch (TimeoutException e) {
            logger.error("TimeoutException in invocationStrategy -- ", e);
        }
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
    }
}
