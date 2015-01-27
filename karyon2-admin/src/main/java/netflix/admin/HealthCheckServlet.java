package netflix.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import netflix.karyon.health.HealthCheckInvocationStrategy;
import org.eclipse.jetty.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Singleton
public class HealthCheckServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckServlet.class);
    private HealthCheckInvocationStrategy invocationStrategy;

    @Inject
    public HealthCheckServlet(HealthCheckInvocationStrategy invocationStrategy) {
        this.invocationStrategy = invocationStrategy;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (null != invocationStrategy) {
            try {
                int status = invocationStrategy.invokeCheck();
                resp.setStatus(status);
            } catch (TimeoutException e) {
                logger.error("TimeoutException in invocationStrategy -- ", e);
                resp.sendError(Response.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.sendError(Response.SC_BAD_REQUEST);
        }
    }
}
