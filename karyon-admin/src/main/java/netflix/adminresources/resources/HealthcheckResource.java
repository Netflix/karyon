package netflix.adminresources.resources;

import com.google.inject.Inject;
import com.sun.jersey.spi.resource.Singleton;
import netflix.karyon.health.HealthCheckInvocationStrategy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeoutException;

/**
 * @author Nitesh Kant
 */
@Path(HealthcheckResource.PATH)
@Singleton
public class HealthcheckResource {

    public static final String PATH = "/healthcheck";

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
