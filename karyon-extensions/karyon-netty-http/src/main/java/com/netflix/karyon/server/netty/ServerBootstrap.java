package com.netflix.karyon.server.netty;

import com.netflix.karyon.server.netty.spi.HttpRequestRouter;
import com.netflix.karyon.server.netty.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootsrap that starts {@link NettyBasedHttpServer}
 *
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class ServerBootstrap {

    private static Logger logger = LoggerFactory.getLogger(ServerBootstrap.class);

    public static void main(String[] args) throws InterruptedException {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

        final NettyBasedHttpServer nettyBasedHttpServer = new NettyBasedHttpServer(8099, new NotFoundRouter());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nettyBasedHttpServer.stop();
                } catch (InterruptedException e) {
                    logger.error("Failed to stop the server", e);
                }
            }
        }));

        nettyBasedHttpServer.start();
    }

    private static class NotFoundRouter implements HttpRequestRouter {

        @Override
        public boolean isBlocking() {
            return false;
        }

        @Override
        public void process(FullHttpRequest request, HttpResponseWriter responseWriter) {
            responseWriter.createResponse(HttpResponseStatus.NOT_FOUND, null);
            responseWriter.sendResponse();
        }
    }
}
