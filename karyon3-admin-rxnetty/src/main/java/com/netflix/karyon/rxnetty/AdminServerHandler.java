package com.netflix.karyon.rxnetty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.netflix.karyon.admin.AdminResource;
import com.netflix.karyon.admin.AdminServer;
import com.netflix.karyon.admin.rest.ResourceContainer;

@Singleton
public class AdminServerHandler implements RequestHandler<ByteBuf, ByteBuf> {
    private static final Logger LOG = LoggerFactory.getLogger(AdminServerHandler.class);
    
    private final Provider<ResourceContainer> controllers;
    private final ObjectMapper mapper;
    private final AdminView view;

    @Inject
    public AdminServerHandler(
            @AdminServer ObjectMapper mapper,
            @AdminServer AdminView view,
            @AdminResource Provider<ResourceContainer> controllers) {
        this.controllers = controllers;
        this.mapper = mapper;
        this.view = view;
    }
    
    @Override
    public Observable<Void> handle(
            HttpServerRequest<ByteBuf> request,
            HttpServerResponse<ByteBuf> response) {
        LOG.info("'{}'", request.getUri());
        
        try {
            if (request.getDecodedPath().equals("/")) {
                String result = view.render(request.getQueryParameters());
                return response
                        .setHeader(HttpHeaders.CONTENT_TYPE, "text/html")
                        .writeStringAndFlushOnEach(Observable.just(result))
                        ;
            }
            else {
                String parts[] = request.getDecodedPath().substring(1).split("/");
                String controller = parts[0];
                List<String> p = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    p.add(parts[i]);
                }
                Object result = controllers.get().invoke(controller, p);
                return response
                        .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                        .writeStringAndFlushOnEach(Observable.just(mapper.writeValueAsString(result)))
                        ;
            }
        }
        catch (Exception e) {
//            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return response
                    .setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
                    .writeStringAndFlushOnEach(Observable.just(sw.toString()));
        }
    }
}
