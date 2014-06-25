package com.netflix.karyon.server.http.spi;

import com.netflix.karyon.server.spi.RequestRouter;
import io.netty.handler.codec.http.HttpObject;

/**
 * A custom router for HTTP based routers. <br/>
 *
 * This typically will be implemented in different modules specific to which framework is used to discover endpoints,
 * eg: Jersey, servlet, custom routing.
 *
 * @author Nitesh Kant
 * @see com.netflix.karyon.server.http
 */
public interface HttpRequestRouter<I extends HttpObject, O extends HttpObject> extends RequestRouter<I, O> {
}
