package com.netflix.karyon.server.http.spi;

import com.netflix.karyon.server.spi.BlockingRequestRouter;
import io.netty.handler.codec.http.HttpObject;

/**
 * An extension of {@link HttpRequestRouter} to explicitly identify blocking routers. <br/>
 * If used, with non-blocking I/O, this router will always be invoked in a different executor. <br/>
 *
 * @author Nitesh Kant
 *
 * @see com.netflix.karyon.server.http
 */
public interface BlockingHttpRequestRouter<I extends HttpObject, O extends HttpObject>
        extends HttpRequestRouter<I, O>, BlockingRequestRouter<I, O> {
}
