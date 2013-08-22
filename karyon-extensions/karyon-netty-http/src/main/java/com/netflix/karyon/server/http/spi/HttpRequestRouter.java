package com.netflix.karyon.server.http.spi;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * A router that decouples actual HTTP request processing from the netty pipeline. <br/>
 * Once the netty request processing pipeline hands-off the processing to this router, the responsibility of the
 * completion of the request i.e. writing the response lies in the hand of this router. <br/>
 *
 * This typically will be implemented in different modules specific to which framework is used to discover endpoints,
 * eg: Jersey, servlet, custom routing.
 *
 * @author Nitesh Kant
 */
public interface HttpRequestRouter {

    /**
     * Returns whether this router is blocking in nature. In case, it is blocking, the framework will make sure that
     * this runs in a different executor than the main event loop to avoid performance degradation.
     *
     * @return {@code true} if this router is blocking {@code false} otherwise.
     */
    boolean isBlocking();

    /**
     * Processes the passed request and writes the response using the passed {@code responseWriter}. <br/>
     *
     * <h2>Errors</h2>
     * This method does not expect implementations to throw errors, in any case, if they do, the error is completely
     * ignored by the framework. This means that the framework will <em>NOT</em> try to write any response back to the
     * client.
     *
     * @param request Request to process.
     * @param responseWriter Response writer to write the response.
     */
    void process(FullHttpRequest request, HttpResponseWriter responseWriter);

    /**
     * Starts the router.
     */
    void start();
}
