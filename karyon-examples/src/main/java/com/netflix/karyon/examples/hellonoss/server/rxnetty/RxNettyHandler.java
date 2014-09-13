package com.netflix.karyon.examples.hellonoss.server.rxnetty;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
public class RxNettyHandler implements RequestHandler<ByteBuf, ByteBuf> {

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        return response.writeStringAndFlush("Hello from Netflix OSS");
    }
}
