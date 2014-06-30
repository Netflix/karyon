package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.base.Joiner;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.junit.Assert;
import org.junit.Test;
import rx.subjects.PublishSubject;

import javax.servlet.Filter;
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

    @SuppressWarnings("PMD") public static final String REMOTE_ADDRESS = "1.1.1.1";
    @SuppressWarnings("PMD") public static final String LOCAL_ADDRESS = "1.0.0.1";
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
        DefaultHttpRequest request = new DefaultHttpRequest(HTTP_VERSION, HTTP_METHOD, testUri);
        HttpServerRequest<ByteBuf> rxRequest = new HttpServerRequest<ByteBuf>(request, PublishSubject.<ByteBuf>create());
        NoOpChannelHandlerContextMock contextMock = new NoOpChannelHandlerContextMock(LOCAL_ADDRESS, SERVER_PORT, LOCAL_ADDRESS,
                                                                                      LOCAL_PORT, REMOTE_ADDRESS,
                                                                                      REMOTE_PORT);

        HttpServerResponse<ByteBuf> rxResponse = new HttpServerResponse<ByteBuf>(contextMock);
        router.route(rxRequest, rxResponse);
        Assert.assertEquals("Unexpected response status.", HttpResponseStatus.NOT_FOUND, rxResponse.getStatus());
    }

    @Test
    public void testRequestCompletnessForMatchingServlet() throws Exception {
        TestableServlet servlet = new TestableServlet();
        TestableFilter filter = new TestableFilter(0, null, false);
        RouterAndAccomplice routerAndAccomplice = new RouterAndAccomplice(servlet, filter).invoke();
        processRequest(routerAndAccomplice);
        Assert.assertTrue("Expected servlet not invoked.", servlet.invoked);
        Assert.assertTrue("Expected filter not invoked.", filter.invoked);
        HttpServletRequest invokedWithReq = servlet.req;
        Assert.assertNotNull("Servlet invoked with null request.", invokedWithReq);
        Assert.assertEquals("Unexpected servlet request path info.", REMAINING_PATH, invokedWithReq.getPathInfo());
        Assert.assertEquals("Unexpected servlet request context path.", CONTEXT_PATH.substring(0, CONTEXT_PATH.length()
                                                                                                  - 1),
                            invokedWithReq.getContextPath());
        Assert.assertEquals("Unexpected servlet request servlet path.", '/' + SERVLET_PATH,
                            invokedWithReq.getServletPath());

        HttpServletResponseImpl invokedWithResp = (HttpServletResponseImpl) servlet.resp;
        Assert.assertEquals("Unexpected response code.", 204, invokedWithResp.serverResponse().getStatus().code());
    }

    @Test
    public void testErrorInFilters() throws Exception {
        TestableServlet servlet = new TestableServlet();
        TestableFilter filter = new TestableFilter(0, null, true);
        RouterAndAccomplice routerAndAccomplice = new RouterAndAccomplice(servlet, filter).invoke();
        processRequest(routerAndAccomplice);
        Assert.assertTrue("Expected filter not invoked.", filter.invoked);
        Assert.assertFalse("Unexpected servlet invoked when filter threw an error.", servlet.invoked);

        HttpServletResponseImpl invokedWithResp = (HttpServletResponseImpl) filter.resp;
        Assert.assertEquals("Unexpected response code.", 500, invokedWithResp.serverResponse().getStatus().code());
    }

    @Test
    public void testErrorInServlet() throws Exception {
        TestableServlet servlet = new TestableServlet(true);
        TestableFilter filter = new TestableFilter(0, null, false);
        RouterAndAccomplice routerAndAccomplice = new RouterAndAccomplice(servlet, filter).invoke();
        processRequest(routerAndAccomplice);
        Assert.assertTrue("Expected filter not invoked.", filter.invoked);
        Assert.assertTrue("Expected servlet not invoked.", servlet.invoked);

        HttpServletResponseImpl invokedWithResp = (HttpServletResponseImpl) filter.resp;
        Assert.assertEquals("Unexpected response code.", 500, invokedWithResp.serverResponse().getStatus().code());
    }

    private void processRequest(RouterAndAccomplice routerAndAccomplice) {
        HttpServletRequestRouter router = routerAndAccomplice.getRouter();
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HTTP_VERSION, HTTP_METHOD, testUri);
        HttpServerRequest<ByteBuf> rxRequest = new HttpServerRequest<ByteBuf>(request, PublishSubject.<ByteBuf>create());
        NoOpChannelHandlerContextMock contextMock = new NoOpChannelHandlerContextMock(LOCAL_ADDRESS, SERVER_PORT, LOCAL_ADDRESS,
                                                                                      LOCAL_PORT, REMOTE_ADDRESS,
                                                                                      REMOTE_PORT);

        HttpServerResponse<ByteBuf> rxResponse = new HttpServerResponse<ByteBuf>(contextMock);
        router.route(rxRequest, rxResponse);
    }

    private class RouterAndAccomplice {

        private final HttpServlet servlet;
        private final Filter filter;
        private HttpServletRequestRouter router;

        private RouterAndAccomplice(HttpServlet servlet, Filter filter) {
            this.servlet = servlet;
            this.filter = filter;
        }

        public HttpServlet getServlet() {
            return servlet;
        }

        public Filter getFilter() {
            return filter;
        }

        public HttpServletRequestRouter getRouter() {
            return router;
        }

        public RouterAndAccomplice invoke() {
            HTTPServletRequestRouterBuilder builder = new HTTPServletRequestRouterBuilder();
            builder.servletFactory(new HTTPServletRequestRouterBuilder.IOCFactory<HttpServlet>() {
                @Override
                public HttpServlet newInstance(Class<HttpServlet> toInstantiate) {
                    return servlet;
                }
            });
            builder.filterFactory(new HTTPServletRequestRouterBuilder.IOCFactory<Filter>() {
                @Override
                public Filter newInstance(Class<Filter> toInstantiate) {
                    return filter;
                }
            });
            builder.contextPath(CONTEXT_PATH);
            builder.forUri(SERVLET_PATH + "/*").serveWith(servlet.getClass());
            builder.forUri(SERVLET_PATH + "/*").filterWith(filter.getClass());
            router = builder.build();
            return this;
        }
    }
}
