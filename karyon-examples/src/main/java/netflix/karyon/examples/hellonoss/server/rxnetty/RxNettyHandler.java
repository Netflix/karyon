package netflix.karyon.examples.hellonoss.server.rxnetty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import netflix.karyon.transport.http.health.HealthCheckEndpoint;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
public class RxNettyHandler implements RequestHandler<ByteBuf, ByteBuf> {

    private final String healthCheckUri;
    private final HealthCheckEndpoint healthCheckEndpoint;

    public RxNettyHandler(String healthCheckUri, HealthCheckEndpoint healthCheckEndpoint) {
        this.healthCheckUri = healthCheckUri;
        this.healthCheckEndpoint = healthCheckEndpoint;
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        if (request.getUri().startsWith(healthCheckUri)) {
            return healthCheckEndpoint.handle(request, response);
        } else if (request.getUri().startsWith("/hello/to/")) {
            int prefixLength = "/hello/to".length();
            String userName = request.getPath().substring(prefixLength);
            if (userName.isEmpty() || userName.length() == 1 /*The uri is /hello/to/ but no name */) {
                response.setStatus(HttpResponseStatus.BAD_REQUEST);
                return response.writeStringAndFlush(
                        "{\"Error\":\"Please provide a username to say hello. The URI should be /hello/to/{username}\"}");
            } else {
                String msg = "Hello " + userName.substring(1) /*Remove the / prefix*/ + " from Netflix OSS";
                return response.writeStringAndFlush("{\"Message\":\"" + msg + "\"}");
            }
        } else if (request.getUri().startsWith("/hello")) {
            return response.writeStringAndFlush("{\"Message\":\"Hello newbee from Netflix OSS\"}");
        } else {
            response.setStatus(HttpResponseStatus.NOT_FOUND);
            return response.close();
        }
    }
}
