package com.netflix.karyon.example.netty;

import com.netflix.karyon.server.netty.NettyBasedHttpServer;
import com.netflix.karyon.server.netty.jersey.blocking.JerseyRouterProvider;
import com.netflix.karyon.server.netty.spi.HttpRequestRouter;

/**
 * @author Nitesh Kant
 */
public class Server {

    public static void main(String[] args) throws InterruptedException {
        HttpRequestRouter router = JerseyRouterProvider.createRouter();
        NettyBasedHttpServer server = new NettyBasedHttpServer(8099, router);
        server.start();
    }
}
