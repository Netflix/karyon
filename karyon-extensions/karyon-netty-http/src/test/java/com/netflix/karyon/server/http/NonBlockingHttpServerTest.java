package com.netflix.karyon.server.http;

import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class NonBlockingHttpServerTest extends HttpServerTestBase {

    @Test
    public void testRouterWithInterceptors() throws Exception {
        int serverPort = 7766;
        NonBlockingHttpServerBuilder builder = new NonBlockingHttpServerBuilder(serverPort);
        configureServerBuilder(builder);

        server = builder.build();
        server.startWithoutWaitingForShutdown();

        makeRootGetCallToLocalServer(serverPort, "/");

        assertRouterInvocation();

        assertInterceptorCalls(uriBasedInterceptor, "Uri based");
        assertInterceptorCalls(regexBasedInterceptor, "Regex based");
        assertInterceptorCalls(methodBasedInterceptor, "Http method based");
    }
}
