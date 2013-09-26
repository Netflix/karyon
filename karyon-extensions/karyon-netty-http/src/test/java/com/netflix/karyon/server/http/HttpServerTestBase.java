package com.netflix.karyon.server.http;

import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.http.interceptor.MethodConstraintKey;
import com.netflix.karyon.server.http.interceptor.RegexUriConstraintKey;
import com.netflix.karyon.server.http.interceptor.ServletStyleUriConstraintKey;
import com.netflix.karyon.server.http.interceptor.TestableBidirectionalInterceptor;
import com.netflix.karyon.spi.PropertyNames;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.io.IOException;

/**
 * @author Nitesh Kant
 */
public class HttpServerTestBase {

    protected TestableBidirectionalInterceptor regexBasedInterceptor;
    protected TestableBidirectionalInterceptor methodBasedInterceptor;
    protected TestableBidirectionalInterceptor uriBasedInterceptor;
    protected TestableRequestRouter router;
    protected HttpServer server;

    @Before
    public void setUp() throws Exception {
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
    }

    @After
    public void tearDown() throws Exception {
        if (null != server) {
            server.stop();
        }
        uriBasedInterceptor = null;
        regexBasedInterceptor = null;
        methodBasedInterceptor = null;
        router = null;
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION);
    }

    protected static void makeRootGetCallToLocalServer(int serverPort, String uri) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet getRequest = new HttpGet("http://localhost:" + serverPort + uri);
        HttpResponse response = client.execute(getRequest);
        Assert.assertEquals("Request " + getRequest + " to the local HTTP server failed.",
                            TestableRequestRouter.ROUTER_RESPONSE_STATUS_DEFAULT.code(),
                            response.getStatusLine().getStatusCode());
    }

    protected static void assertInterceptorCalls(TestableBidirectionalInterceptor interceptor, String name) {
        Assert.assertTrue(name + " interceptor did not get invoked for inbound processing.",
                          interceptor.isCalledForIn());
        Assert.assertTrue(name + " interceptor got invoked for inbound processing but was not supposed to be.", interceptor.wasLastInCallValid());
        Assert.assertTrue(name + " interceptor did not get invoked for outbound processing.", interceptor.isCalledForOut());
        Assert.assertTrue(name + " interceptor got invoked for outbound processing but was not supposed to be.",
                          interceptor.wasLastOutCallValid());
    }

    protected void configureServerBuilder(@SuppressWarnings("rawtypes") HttpServerBuilder builder) {
        router = new TestableRequestRouter();
        regexBasedInterceptor = new TestableBidirectionalInterceptor(new RegexUriConstraintKey("/.*"));
        methodBasedInterceptor = new TestableBidirectionalInterceptor(new MethodConstraintKey(HttpMethod.GET));
        uriBasedInterceptor = new TestableBidirectionalInterceptor(new ServletStyleUriConstraintKey("/*"));
        builder.requestRouter(router)
               .forUri("/*").interceptWith(uriBasedInterceptor)
               .forUriRegex("/.*").interceptWith(regexBasedInterceptor)
               .forHttpMethod(HttpMethod.GET).interceptWith(methodBasedInterceptor);
    }

    protected void assertRouterInvocation() {
        Assert.assertTrue("Router did not get invoked.", router.isExecuted());
    }
}
