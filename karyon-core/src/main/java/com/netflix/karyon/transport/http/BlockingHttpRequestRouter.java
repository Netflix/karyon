package com.netflix.karyon.transport.http;

import io.reactivex.netty.protocol.http.server.RequestHandler;

/**
 * An interface for blocking HTTP based routers.
 *
 * @author Nitesh Kant
 */
public interface BlockingHttpRequestRouter<I, O> extends RequestHandler<I, O> {
}
