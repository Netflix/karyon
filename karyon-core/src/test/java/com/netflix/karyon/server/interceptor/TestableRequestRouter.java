package com.netflix.karyon.server.interceptor;

import com.netflix.karyon.transport.RequestRouter;
import rx.Observable;

/**
* @author Nitesh Kant
*/
class TestableRequestRouter<I, O> implements RequestRouter<I, O> {

    private volatile boolean called;

    @Override
    public Observable<Void> route(I request, O response) {
        called = true;
        return Observable.empty();
    }

    public boolean isReceivedACall() {
        return called;
    }
}
