package com.netflix.karyon.server.http;

import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.http.interceptor.MethodConstraintKey;
import com.netflix.karyon.server.http.interceptor.RegexUriConstraintKey;
import com.netflix.karyon.server.http.interceptor.ServletStyleUriConstraintKey;
import com.netflix.karyon.server.http.interceptor.TestableBidirectionalInterceptor;
import com.netflix.karyon.server.spi.DefaultChannelPipelineConfigurator;
import com.netflix.karyon.spi.PropertyNames;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
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

    protected TestableBidirectionalInterceptor<FullHttpRequest, FullHttpResponse> regexBasedInterceptor;
    protected TestableBidirectionalInterceptor<FullHttpRequest, FullHttpResponse> methodBasedInterceptor;
    protected TestableBidirectionalInterceptor<FullHttpRequest, FullHttpResponse> uriBasedInterceptor;
    protected TestableRequestRouter router;
    protected HttpServer<FullHttpRequest, FullHttpResponse> server;

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

    protected static void assertInterceptorCalls(TestableBidirectionalInterceptor<FullHttpRequest, FullHttpResponse> interceptor,
                                                 String name) {
        Assert.assertTrue(name + " interceptor did not get invoked for inbound processing.",
                          interceptor.isCalledForIn());
        Assert.assertTrue(name + " interceptor got invoked for inbound processing but was not supposed to be.", interceptor.wasLastInCallValid());
        Assert.assertTrue(name + " interceptor did not get invoked for outbound processing.", interceptor.isCalledForOut());
        Assert.assertTrue(name + " interceptor got invoked for outbound processing but was not supposed to be.",
                          interceptor.wasLastOutCallValid());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void configureServerBuilder(HttpServerBuilder builder) {
        router = new TestableRequestRouter();
        HttpPipelineConfigurator httpConfigurator = new HttpPipelineConfigurator();
        DefaultChannelPipelineConfigurator<FullHttpRequest, FullHttpResponse> pipelineConfigurator =
                new DefaultChannelPipelineConfigurator<FullHttpRequest, FullHttpResponse>(builder.getUniqueServerName(),
                                                                                          null,
                                                                                          new FullHttpObjectPipelineConfiguratorImpl(8192, httpConfigurator));
        builder.requestRouter(router).pipelineConfigurator(pipelineConfigurator)
               .responseWriterFactory(new StatefulHttpResponseWriterImpl.ResponseWriterFactoryImpl());
        HttpServerBuilder.InterceptorAttacher uriAttacher = builder.forUri("/*");
        HttpServerBuilder.InterceptorAttacher uriRegexAttacher = builder.forUriRegex("/.*");
        HttpServerBuilder.InterceptorAttacher methodAttacher = builder.forHttpMethod(HttpMethod.GET);
        regexBasedInterceptor = new TestableBidirectionalInterceptor(new RegexUriConstraintKey("/.*"), uriRegexAttacher);
        methodBasedInterceptor = new TestableBidirectionalInterceptor(new MethodConstraintKey(HttpMethod.GET),
                                                                      methodAttacher);
        uriBasedInterceptor = new TestableBidirectionalInterceptor(new ServletStyleUriConstraintKey("/*", ""),
                                                                   uriAttacher);
    }

    protected void assertRouterInvocation() {
        Assert.assertTrue("Router did not get invoked.", router.isExecuted());
    }
}
