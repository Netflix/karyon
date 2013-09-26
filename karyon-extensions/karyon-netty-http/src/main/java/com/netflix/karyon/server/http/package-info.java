/**
 * This package provides two primary ways of starting an HTTP server, viz:
 * <ul>
 <li>{@link BlockingHttpServer} created using {@link BlockingHttpServerBuilder}</li>
 <li>{@link NonBlockingHttpServer } created using {@link NonBlockingHttpServerBuilder}</li>
 </ul>
 *
 * <h1>Architecture</h1>
 *
 * The below is the general architecture of an HTTP server created using this package.
 *
 <PRE>
 *  +---------------+-----------------------------------+------------------------------+
 *  |                                                                                  |
 *  |             {@link HttpRequestRouter} (JAX-RS, Servlet, User defined, etc.)      |
 *  +----------------------------------------------------------------------------------+
 *                  |                                              |
 *  +---------------------------------------------------+------------------------------+
 *  |              /|\                                             |                   |
 *  |               |                                             \|/                  |
 *  |    +----------+-----------------+            +-----------+-------------------+   |
 *  |    |{@link InboundInterceptor} n|            | {@link OutboundInterceptor} 1 |   |
 *  |    +----------+-----------------+            +-----------+-------------------+   |
 *  |              /|\                                              |                  |
    |        {@link InterceptorExecutionContext#executeNextInterceptor(FullHttpRequest,|
    |                              HttpResponseWriter)}                                |
 *  |               |                                               |                  |
 *  |               |                                              \|/                 |
 *  |    +----------+-----------------+            +-----------+-------------------+   |
 *  |    |{@link InboundInterceptor} 1|            | {@link OutboundInterceptor} 1 |   |
 *  |    +----------+-----------------+            +-----------+-------------------+   |
 *  |              /|\                                              |                  |
 *  |               |                   Karyon Server               |                  |
 *  |    +----------+-----------------------------------------------+--------------+   |
 *  |    |            Executor pool iff the {@link HttpRequestRouter} is           |   |
 *  |    |                blocking and server is non blocking                      |   |
 *  |    +----------+-----------------------------------------------+--------------+   |
 *  |               |                                               |                  |
 *  +---------------+-----------------------------------------------+------------------+
 *                  |                                              \|/
 *  +---------------+-----------------------------------------------+------------------+
 *  |              /|\                                              |                  |
 *  |               |   Netty framework (I/O & Transport handling)  |                  |
 *  |               |                                              \|                  |
 *  |    +----------+-----------------------------------------------+--------------+   |
 *  |    |           1 Netty event loop group both for accepting new connections   |   |
 *  |    |                       as well as serving client connections.            |   |
 *  |    +----------+-----------------------------------------------+--------------+   |                                                                                 |
 *  +---------------+-----------------------------------------------+------------------+
 </PRE>
 */
package com.netflix.karyon.server.http;

import com.netflix.karyon.server.http.interceptor.InboundInterceptor;
import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContext;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;