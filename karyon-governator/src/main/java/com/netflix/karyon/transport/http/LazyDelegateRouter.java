package com.netflix.karyon.transport.http;

import com.google.inject.Injector;

/**
 * @author Nitesh Kant
 */
public interface LazyDelegateRouter<I, O> extends HttpRequestRouter<I, O> {

    void setRouter(Injector injector, HttpRequestRouter<I, O> delegate);

}
