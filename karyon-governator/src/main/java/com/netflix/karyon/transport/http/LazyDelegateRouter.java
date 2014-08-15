package com.netflix.karyon.transport.http;

import com.google.inject.Injector;
import io.reactivex.netty.protocol.http.server.RequestHandler;

/**
 * @author Nitesh Kant
 */
public interface LazyDelegateRouter<I, O> extends RequestHandler<I, O> {

    void setRouter(Injector injector, RequestHandler<I, O> delegate);

}
