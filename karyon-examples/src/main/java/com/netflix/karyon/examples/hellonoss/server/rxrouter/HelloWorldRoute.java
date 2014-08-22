package com.netflix.karyon.examples.hellonoss.server.rxrouter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.channel.StringTransformer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * @author Tomasz Bak
 */
@Singleton
public class HelloWorldRoute implements HttpRequestRouter<ByteBuf, ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldRoute.class);
    private final HealthCheckHandler healthCheckHandler;

    @Inject
    public HelloWorldRoute(HealthCheckHandler healthCheckHandler) {
        this.healthCheckHandler = healthCheckHandler;
    }

    @Override
    public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        String path = request.getPath();

        if (HttpMethod.GET.equals(request.getHttpMethod())) {
            // /hello
            if ("/hello".equals(path) || "/hello/".equals(path)) {
                return handleHello(response);
            }
            // /hello/to/{name}
            String name;
            if (path.startsWith("/hello/to/") && !(name = path.substring("/hello/to/".length())).isEmpty()) {
                return handleHelloUser(response, name);
            }
            // healthcheck
            if ("/healthcheck".equals(path)) {
                return handleHealthCheck(response);
            }
        }

        response.setStatus(HttpResponseStatus.NOT_FOUND);
        return response.close();
    }

    private Observable<Void> handleHello(HttpServerResponse<ByteBuf> httpResponse) {
        JSONObject response = new JSONObject();
        try {
            response.put("Message", "Hello from Netflix OSS");
            httpResponse.writeAndFlush(response.toString(), StringTransformer.DEFAULT_INSTANCE);
            httpResponse.setStatus(HttpResponseStatus.OK);
            return Observable.empty();
        } catch (JSONException e) {
            logger.error("Error creating json response.", e);
            httpResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return Observable.error(e);
        }
    }

    private Observable<Void> handleHelloUser(HttpServerResponse<ByteBuf> httpResponse, String name) {
        JSONObject response = new JSONObject();
        try {
            response.put("Message", "Hello " + name + " from Netflix OSS");
            httpResponse.writeAndFlush(response.toString(), StringTransformer.DEFAULT_INSTANCE);
            httpResponse.setStatus(HttpResponseStatus.OK);
            return Observable.empty();
        } catch (JSONException e) {
            logger.error("Error creating json response.", e);
            httpResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return Observable.error(e);
        }

    }

    private Observable<Void> handleHealthCheck(HttpServerResponse<ByteBuf> httpResponse) {
        JSONObject response = new JSONObject();
        try {
            response.put("Status", healthCheckHandler.getStatus());
            httpResponse.writeAndFlush(response.toString(), StringTransformer.DEFAULT_INSTANCE);
            httpResponse.setStatus(HttpResponseStatus.OK);
            return Observable.empty();
        } catch (JSONException e) {
            logger.error("Error creating json response.", e);
            httpResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            return Observable.error(e);
        }
    }
}
