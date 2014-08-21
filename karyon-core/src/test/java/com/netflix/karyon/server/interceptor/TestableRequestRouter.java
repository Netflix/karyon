package com.netflix.karyon.server.interceptor;

import io.reactivex.netty.channel.Handler;
import rx.Observable;

/**
* @author Nitesh Kant
*/
class TestableRequestRouter<I, O> implements Handler<I, O> {

    private volatile boolean called;

    @Override
    public Observable<Void> handle(I request, O response) {
        called = true;
        return Observable.empty();
    }

    public boolean isReceivedACall() {
        return called;
    }
}
