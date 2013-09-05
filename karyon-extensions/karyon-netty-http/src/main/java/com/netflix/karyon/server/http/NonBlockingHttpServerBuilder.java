package com.netflix.karyon.server.http;

import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * A builder to create a {@link NonBlockingHttpServer} instance. <br/>
 *
 * <h2>Event loops</h2>
 *
 * Every HttpServer requires two event loop groups, one for accepting new connections and another for serving a
 * particular client connection. This builder uses the same event loop group for both these event loops.<br/>
 * Since, this server only serves a single server port, there is always a single event loop required for accepting
 * new client connections. So, there is hardly ever a reason to specify more than one event loop for accepting new
 * connections & hence this builder does not allow configuring one.
 *
 * @see com.netflix.karyon.server.http
 *
 * @author Nitesh Kant
 */
public class NonBlockingHttpServerBuilder extends HttpServerBuilder<NonBlockingHttpServerBuilder, NonBlockingHttpServer> {

    private static final int NOT_DEFINED = -1;

    private int selectorCount;
    private int executorThreadCount;

    public NonBlockingHttpServerBuilder(int serverPort) {
        super(serverPort);
    }

    public NonBlockingHttpServerBuilder withSelectorCount(int selectorCount) {
        this.selectorCount = selectorCount;
        return this;
    }

    public NonBlockingHttpServerBuilder requestRouter(HttpRequestRouter requestRouter, int executorThreadCount) {
        this.requestRouter = requestRouter;
        this.executorThreadCount = executorThreadCount;
        return this;
    }

    @Override
    protected void validate() {
        super.validate();
        if(requestRouter.isBlocking() && executorThreadCount == NOT_DEFINED) {
            throw new IllegalArgumentException("Blocking request router must specify executor thread count.");
        }

    }

    @Override
    protected NonBlockingHttpServer createServer() {
        return new NonBlockingHttpServer(nettyBootstrap, requestRouter, executorThreadCount, interceptorFactory,
                                         karyonBootstrap);
    }

    @Override
    protected void configureBootstrap() {
        nettyBootstrap.group(new NioEventLoopGroup(selectorCount))
                      .channel(NioServerSocketChannel.class);
    }
}
