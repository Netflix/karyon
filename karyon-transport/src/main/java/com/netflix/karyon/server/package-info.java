/**
 * This package provides two primary ways of starting a netty based karyon server, viz:
 * <ul>
 <li>{@link BlockingKaryonServer} created using {@link BlockingKaryonServerBuilder}</li>
 <li>{@link NonBlockingKaryonServer } created using {@link NonBlockingKaryonServerBuilder}</li>
 </ul>
 *
 * <h1>Architecture</h1>
 *
 * The below is the general architecture of a karyon server created using this package.
 *
 <PRE>
 *  +---------------+-----------------------------------+------------------------------+
 *  |                                                                                  |
 *  |             {@link RequestRouter} (JAX-RS, Servlet, User defined, etc.)      |
 *  +----------------------------------------------------------------------------------+
 *                  |                                               |
 *  +---------------------------------------------------+------------------------------+
 *  |              /|\                                              |                  |
 *  |               |                   Karyon Server               |                  |
 *  |    +----------+-----------------------------------------------+--------------+   |
 *  |    |            Executor pool iff the {@link RequestRouter} is           |   |
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
package com.netflix.karyon.server;

