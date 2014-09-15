package com.netflix.karyon.examples.hellonoss.server.simple.module;

import com.netflix.karyon.examples.hellonoss.common.LoggingInterceptor;
import com.netflix.karyon.examples.hellonoss.common.auth.AuthInterceptor;
import com.netflix.karyon.examples.hellonoss.common.auth.AuthenticationServiceImpl;
import com.netflix.karyon.examples.hellonoss.server.simple.SimpleRouter;
import com.netflix.karyon.transport.http.HttpInterceptorSupport;
import com.netflix.karyon.transport.http.HttpRequestHandler;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
public class RouterWithInterceptors implements RequestHandler<ByteBuf, ByteBuf> {

    private final HttpRequestHandler<ByteBuf, ByteBuf> delegate;

    public RouterWithInterceptors() {
        SimpleRouter router = new SimpleRouter();
        HttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport = new HttpInterceptorSupport<ByteBuf, ByteBuf>();
        interceptorSupport.forUri("/*").intercept(new LoggingInterceptor());
        interceptorSupport.forUri("/hello").intercept(new AuthInterceptor(new AuthenticationServiceImpl()));
        delegate = new HttpRequestHandler<ByteBuf, ByteBuf>(router, interceptorSupport);
    }

    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        return delegate.handle(request, response);
    }
}
