package com.netflix.karyon.rxnetty.server;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

/**
 * Very basic URL routing using a set of HttpEndpointDefinition to specify the routes and 
 * associated handler.
 * 
 * @author elandau
 *
 */
public class HttpRoutingRequestHandler implements RequestHandler<ByteBuf, ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(HttpRoutingRequestHandler.class);
    
    private final List<HttpEndpointDefinition> defs = new ArrayList<>();
    
    public HttpRoutingRequestHandler(Set<HttpEndpointDefinition> endpoints) throws Exception {
        defs.addAll(endpoints);
    }
    
    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        LOG.debug("Access: {} {}", request.getHttpMethod(), request.getPath());
        String uri = request.getPath();
        
        for (HttpEndpointDefinition def : defs) {
            if (def.shouldServe(uri)) {
                return def.serve(request, response);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Routing not found for : ").append(uri).append("\n")
          .append("Available routes are:\n");
        for (HttpEndpointDefinition def : defs) {
            sb.append(def.getPattern()).append("\n");
        }
        response.setStatus(HttpResponseStatus.NOT_FOUND);
        return response.writeStringAndFlush(sb.toString());
    }
}
