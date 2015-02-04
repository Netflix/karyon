package netflix.adminresources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import netflix.karyon.health.HealthCheckHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/healthcheck")
@Produces(MediaType.TEXT_HTML)
@Singleton
public class HealthCheckResource {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckResource.class);
    private HealthCheckHandler healthCheckHandler;

    @Inject
    public HealthCheckResource(HealthCheckHandler healthCheckHandler) {
        this.healthCheckHandler = healthCheckHandler;
    }

    @GET
    public Response getHealthCheck() {
        try {
            final int status = healthCheckHandler.getStatus();
            return Response.ok().status(status).build();
        } catch (Exception e) {
            logger.error("Exception in HealthCheckResource -- ", e);
        }
        return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
    }
}
