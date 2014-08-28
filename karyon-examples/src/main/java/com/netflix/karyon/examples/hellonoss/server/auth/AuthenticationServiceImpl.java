package com.netflix.karyon.examples.hellonoss.server.auth;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    public static final String AUTH_HEADER_NAME = "MY-USER-ID";

    @Override
    public Observable<Boolean> authenticate(HttpServerRequest<ByteBuf> request) {
        if (request.getHeaders().contains(AUTH_HEADER_NAME)) {
            return Observable.from(Boolean.TRUE);
        } else {
            return Observable.error(new IllegalArgumentException("Should pass a header: " + AUTH_HEADER_NAME));
        }
    }
}
