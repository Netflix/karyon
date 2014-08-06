package com.netflix.hellonoss.server.auth;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
public interface AuthenticationService {

    Observable<Boolean> authenticate(HttpServerRequest<ByteBuf> request);
}
