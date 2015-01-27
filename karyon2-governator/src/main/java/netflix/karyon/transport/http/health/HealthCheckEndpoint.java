package netflix.karyon.transport.http.health;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.health.HealthCheckHandler;
import rx.Observable;

import javax.inject.Inject;

/**
 * An implementation of {@link RequestHandler} to provide health status over HTTP.
 *
 * This endpoint does <b>NOT</b> validate whether the passed request URI is actually of healthcheck or not. It assumes
 * that it is used by a higher level router that makes appropriate decisions of which handler to call based on the URI.
 *
 * @author Nitesh Kant
 */
public class HealthCheckEndpoint implements RequestHandler<ByteBuf, ByteBuf> {

    private final HealthCheckHandler healthCheckHandler;

    @Inject
    public HealthCheckEndpoint(HealthCheckHandler healthCheckHandler) {
        this.healthCheckHandler = healthCheckHandler;
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        int httpStatus = healthCheckHandler.getStatus();
        response.setStatus(HttpResponseStatus.valueOf(httpStatus));
        return response.close();
    }
}
