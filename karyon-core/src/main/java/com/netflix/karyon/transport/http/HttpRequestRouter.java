package com.netflix.karyon.transport.http;

import com.netflix.karyon.transport.RequestRouter;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;

/**
 * An interface for HTTP based {@link RequestRouter}s.
 *
 * @author Nitesh Kant
 *
 * @deprecated Use RxNetty's {@link RequestHandler} instead.
 */
@Deprecated
public interface HttpRequestRouter<I, O> extends RequestRouter<HttpServerRequest<I>, HttpServerResponse<O>> {
}
