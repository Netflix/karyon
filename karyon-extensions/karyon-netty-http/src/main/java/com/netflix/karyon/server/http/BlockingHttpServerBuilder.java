package com.netflix.karyon.server.http;

import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.oio.OioServerSocketChannel;

/**
 * A builder to create a {@link BlockingHttpServer} instance. <br/>
 *
 * <h2>Event loops</h2>
 *
 * Every HttpServer requires two event loop groups, one for accepting new connections and another for serving a
 * particular client connection. By default, this builder uses the same event loop group for both these event loops.<br/>
 * Since, this server only serves a single server port, there is always a single event loop required for accepting
 * new client connections. So, there is hardly ever a reason to specify more than one event loop for accepting new
 * connections & hence this builder does not allow configuring one.
 *
 * @see com.netflix.karyon.server.http
 *
 * @author Nitesh Kant
 */
public class BlockingHttpServerBuilder extends HttpServerBuilder<BlockingHttpServerBuilder, BlockingHttpServer> {

    private int workerCount = 200;

    public BlockingHttpServerBuilder(int serverPort) {
        super(serverPort);
    }

    public BlockingHttpServerBuilder withWorkerCount(int workerCount) {
        this.workerCount = workerCount;
        return this;
    }

    @Override
    protected BlockingHttpServer createServer() {
        return new BlockingHttpServer(nettyBootstrap, requestRouter, interceptorFactory, karyonBootstrap);
    }

    @Override
    protected void configureBootstrap() {
        nettyBootstrap.group(new OioEventLoopGroup(workerCount))
                      .channel(OioServerSocketChannel.class);
    }

}
