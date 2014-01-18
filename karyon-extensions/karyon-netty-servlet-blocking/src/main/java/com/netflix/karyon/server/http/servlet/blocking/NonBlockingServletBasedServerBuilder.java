package com.netflix.karyon.server.http.servlet.blocking;

import com.netflix.karyon.server.http.NonBlockingHttpServerBuilder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.logging.LogLevel;

import javax.annotation.Nullable;

/**
 * @author Nitesh Kant
 */
public class NonBlockingServletBasedServerBuilder extends NonBlockingHttpServerBuilder<FullHttpRequest, FullHttpResponse> {

    public NonBlockingServletBasedServerBuilder(int serverPort) {
        this(serverPort, null);
    }

    public NonBlockingServletBasedServerBuilder(int serverPort, @Nullable LogLevel nettyLoggerLevel) {
        super(serverPort, nettyLoggerLevel);
    }
}
