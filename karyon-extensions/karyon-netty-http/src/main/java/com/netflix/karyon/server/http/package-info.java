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
 *  |    |    InboundInterceptor n    |            |     OutboundInterceptor 1     |   |
 *  |    +----------+-----------------+            +-----------+-------------------+   |
 *  |              /|\                                              |                  |
    |              InterceptorExecutionContext.executeNextInterceptor()                |
 *  |               |                                               |                  |
 *  |               |                                              \|/                 |
 *  |    +----------+-----------------+            +-----------+-------------------+   |
 *  |    |   InboundInterceptor 1     |            |     OutboundInterceptor n     |   |
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

