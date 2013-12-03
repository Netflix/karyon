package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.base.Joiner;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author Nitesh Kant
 */
public class RouterTest {

    public static final String CONTEXT_PATH = "/REST/v1/";
    public static final String SERVLET_PATH = "hello";
    public static final String REMAINING_PATH = "/abc&def";
    public static final String QUERY_PARAM_1_NAME = "id";
    public static final String QUERY_PARAM_1_VALUE = "1";
    public static final String QUERY_PARAM_2_NAME = "name";
    public static final String QUERY_PARAM_2_VALUE = "xyz";
    public static final String QUERY_STR = Joiner.on('&')
                                                 .join(Joiner.on('=').join(QUERY_PARAM_1_NAME, QUERY_PARAM_1_VALUE),
                                                       Joiner.on('=').join(QUERY_PARAM_2_NAME, QUERY_PARAM_2_VALUE));
    public static final HttpMethod HTTP_METHOD = HttpMethod.GET;
    public static final HttpVersion HTTP_VERSION = HttpVersion.HTTP_1_1;
    public static final String JSESSIONID_COOKIE_NAME = "JSESSIONID";
    public static final String REMOTE_ADDRESS = "1.1.1.1";
    public static final String LOCAL_ADDRESS = "1.0.0.1";
    public static final int SERVER_PORT = 9999;
    public static final int LOCAL_PORT = 9998;
    public static final int REMOTE_PORT = 8888;
    private static String testUri;

    static {
        final String uriNoEncode = CONTEXT_PATH + SERVLET_PATH + REMAINING_PATH + '?' + QUERY_STR;
        try {
            testUri = URLEncoder.encode(uriNoEncode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            testUri = uriNoEncode;
        }
    }

    @Test
    public void testNotFound() throws Exception {
        HTTPServletRequestRouterBuilder builder = new HTTPServletRequestRouterBuilder();
        HttpServletRequestRouter router = builder.build();
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_VERSION, HTTP_METHOD, testUri);
        HttpResponseWriterMock writerMock = getHttpResponseWriterMock();
        router.process(request, writerMock);

        FullHttpResponse response = writerMock.response();
        Assert.assertNotNull("Response is null.", response);
        Assert.assertEquals("Unexpected response status.", HttpResponseStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    public void testRequestCompletnessForMatchingServlet() throws Exception {
        HTTPServletRequestRouterBuilder builder = new HTTPServletRequestRouterBuilder();
        final TestableServlet servlet = new TestableServlet();
        builder.servletFactory(new HTTPServletRequestRouterBuilder.IOCFactory<HttpServlet>() {
            @Override
            public HttpServlet newInstance(Class<HttpServlet> toInstantiate) {
                return servlet;
            }
        });
        builder.contextPath(CONTEXT_PATH);
        builder.forUri(SERVLET_PATH + "/*").serveWith(TestableServlet.class);
        HttpServletRequestRouter router = builder.build();
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_VERSION, HTTP_METHOD, testUri);
        HttpResponseWriterMock writerMock = getHttpResponseWriterMock();
        router.process(request, writerMock);
        Assert.assertTrue("Expected servlet not invoked.", servlet.invoked);
        HttpServletRequest invokedWithReq = servlet.req;
        Assert.assertNotNull("Servlet invoked with null request.", invokedWithReq);
        Assert.assertEquals("Unexpected servlet request path info.", REMAINING_PATH, invokedWithReq.getPathInfo());
        Assert.assertEquals("Unexpected servlet request context path.", CONTEXT_PATH.substring(0, CONTEXT_PATH.length() - 1),
                            invokedWithReq.getContextPath());
        Assert.assertEquals("Unexpected servlet request servlet path.", '/' + SERVLET_PATH, invokedWithReq.getServletPath());
    }

    private static HttpResponseWriterMock getHttpResponseWriterMock() {
        ChannelHandlerContextMock contextMock = new ChannelHandlerContextMock(LOCAL_ADDRESS, SERVER_PORT, LOCAL_ADDRESS,
                                                                              LOCAL_PORT, REMOTE_ADDRESS,
                                                                              REMOTE_PORT);
        return new HttpResponseWriterMock(HTTP_VERSION, contextMock);
    }
}
