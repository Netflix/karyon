package com.netflix.karyon.server.netty;

import com.netflix.karyon.server.netty.spi.HttpRequestRouter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NettyBasedHttpServer /*extends KaryonServer*/ {

    private Logger logger = LoggerFactory.getLogger(NettyBasedHttpServer.class);

    private final int port;
    private final HttpRequestRouter httpRequestRouter;
    private ServerBootstrap bootstrap;
    private NioEventLoopGroup acceptorGroup;
    private NioEventLoopGroup workerGroup;

    public NettyBasedHttpServer(int port, HttpRequestRouter httpRequestRouter) {
        this.port = port;
        this.httpRequestRouter = httpRequestRouter;
    }

    public void start() throws InterruptedException {
        bootstrap = new ServerBootstrap();
        acceptorGroup = new NioEventLoopGroup(1); // TODO: Externalize these configs
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(acceptorGroup, workerGroup)
                 .channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100)// TODO: Externalize these configs
                 .localAddress(port)
                 .childOption(ChannelOption.TCP_NODELAY, true)
                 .childHandler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws Exception {
                         ch.pipeline()
                           .addLast("logger", new LoggingHandler())
                           .addLast("decoder", new HttpRequestDecoder())
                           .addLast("aggregator", new HttpObjectAggregator(1048576))
                           .addLast("encoder", new HttpResponseEncoder())
                           .addLast("", new ServerHandler(httpRequestRouter));
                     }
                 });
        Channel channel = bootstrap.bind().sync().channel();
        logger.info("Started netty server at port: " + port);
        channel.closeFuture().sync(); // Blocking till it closes
    }

    public void stop() throws InterruptedException {
        acceptorGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        acceptorGroup.terminationFuture().sync();
        workerGroup.terminationFuture().sync();
    }
}
