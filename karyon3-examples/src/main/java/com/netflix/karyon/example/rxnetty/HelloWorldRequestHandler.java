package com.netflix.karyon.example.rxnetty;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;

public class HelloWorldRequestHandler implements RequestHandler<ByteBuf, ByteBuf> {
    final AtomicInteger counter = new AtomicInteger();

    @Override
    public Observable<Void> handle(
            HttpServerRequest<ByteBuf> request,
            HttpServerResponse<ByteBuf> response) {
        int count = counter.incrementAndGet();
        return response.writeString(Observable.just("Hello World " + count + "!"));
    }
}
