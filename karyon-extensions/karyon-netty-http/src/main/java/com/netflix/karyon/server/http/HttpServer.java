package com.netflix.karyon.server.http;

import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class HttpServer extends KaryonServer {

    private final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    protected final HttpRequestRouter httpRequestRouter;
    private ServerBootstrap bootstrap;

    HttpServer(@Nonnull ServerBootstrap bootstrap,
               @Nonnull HttpRequestRouter httpRequestRouter,
               @Nullable com.netflix.karyon.server.ServerBootstrap karyonBootstrap) {
        super(karyonBootstrap);
        this.bootstrap = bootstrap;
        this.httpRequestRouter = httpRequestRouter;
    }

    @Override
    public void start() throws Exception {
        initialize();
        super.start();
        httpRequestRouter.start();
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                  .addLast("logger", new LoggingHandler())
                  .addLast("decoder", new HttpRequestDecoder())
                  .addLast("aggregator", new HttpObjectAggregator(1048576))
                  .addLast("encoder", new HttpResponseEncoder());
                addRouterToPipeline(ch);
            }
        });
        Channel channel = bootstrap.bind().sync().channel();
        logger.info("Started netty http module at port: " + channel.localAddress());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    stop();
                } catch (InterruptedException e) {
                    logger.error("Error while shutting down.", e);
                }
            }
        }));
        channel.closeFuture().sync(); // Blocking till it closes
    }

    public void stop() throws InterruptedException {

        logger.info("Shutting down server.");
        Future<?> acceptorTermFuture = bootstrap.group().shutdownGracefully();
        Future<?> workerTermFuture = bootstrap.childGroup().shutdownGracefully();

        logger.info("Waiting for acceptor threads to stop.");
        acceptorTermFuture.sync();
        logger.info("Waiting for worker threads to stop.");
        workerTermFuture.sync();
        logger.info("Shutdown complete.");
    }

    protected abstract void addRouterToPipeline(SocketChannel ch);
}
