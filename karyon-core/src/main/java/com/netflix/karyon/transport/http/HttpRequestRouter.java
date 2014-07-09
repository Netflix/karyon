package com.netflix.karyon.transport.http;

import com.netflix.karyon.transport.RequestRouter;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

/**
 * An interface for HTTP based {@link RequestRouter}s.
 *
 * @author Nitesh Kant
 */
public interface HttpRequestRouter<I, O> extends RequestRouter<HttpServerRequest<I>, HttpServerResponse<O>> {
}
