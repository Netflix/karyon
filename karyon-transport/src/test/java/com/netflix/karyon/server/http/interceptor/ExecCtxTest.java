package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nitesh Kant
 */
public class ExecCtxTest {

    @Test
    public void testInboundCtx() throws Exception {
        List<InboundInterceptor<FullHttpRequest, FullHttpResponse>> interceptors =
                new ArrayList<InboundInterceptor<FullHttpRequest, FullHttpResponse>>();
        TestableInboundInterceptor intercept1 = new TestableInboundInterceptor(new ServletStyleUriConstraintKey("*", ""));
        TestableInboundInterceptor intercept2 = new TestableInboundInterceptor(new ServletStyleUriConstraintKey("*", ""));
        TestableInboundInterceptor intercept3 = new TestableInboundInterceptor(new ServletStyleUriConstraintKey("*", ""));
        TestableInboundInterceptor tail = new TestableInboundInterceptor(new ServletStyleUriConstraintKey("*", ""));
        interceptors.add(intercept1);
        interceptors.add(intercept2);
        interceptors.add(intercept3);

        NextInterceptorInvoker<FullHttpRequest, FullHttpResponse> ctx =
                new InboundNextInterceptorInvoker<FullHttpRequest, FullHttpResponse>(interceptors, tail);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def"); // contents do not matter as we do not run filtering
        ctx.executeNext(request, new StatefulHttpResponseWriterMock());


        Assert.assertTrue("1st interceptor did not get invoked.", intercept1.isReceivedACall());
        Assert.assertTrue("2rd interceptor did not get invoked.", intercept2.isReceivedACall());
        Assert.assertTrue("3rd interceptor did not get invoked.", intercept3.isReceivedACall());
        Assert.assertTrue("Tail interceptor did not get invoked.", tail.isReceivedACall());
    }

    @Test
    public void testOutboundCtx() throws Exception {

        FullHttpRequest testRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/");

        List<OutboundInterceptor<FullHttpResponse>> interceptors =
                new ArrayList<OutboundInterceptor<FullHttpResponse>>();
        TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> intercept1 =
                new TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse>(new ServletStyleUriConstraintKey("*",
                                                                                                                    ""));
        intercept1.setLastInCallRequest(testRequest);
        TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> intercept2 =
                new TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse>(new ServletStyleUriConstraintKey("*",
                                                                                                                    ""));
        intercept2.setLastInCallRequest(testRequest);
        TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> intercept3 =
                new TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse>(new ServletStyleUriConstraintKey("*",
                                                                                                                    ""));
        intercept3.setLastInCallRequest(testRequest);
        TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> tail =
                new TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse>(new ServletStyleUriConstraintKey("*",
                                                                                                                    ""));
        tail.setLastInCallRequest(testRequest);

        interceptors.add(intercept1);
        interceptors.add(intercept2);
        interceptors.add(intercept3);

        NextInterceptorInvoker<FullHttpResponse, FullHttpResponse> ctx =
                new OutboundNextInterceptorInvoker<FullHttpResponse>(interceptors, tail);
        FullHttpResponse request = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        ctx.executeNext(request, new StatefulHttpResponseWriterMock());


        Assert.assertTrue("1st interceptor did not get invoked.", intercept1.isReceivedACall());
        Assert.assertTrue("2rd interceptor did not get invoked.", intercept2.isReceivedACall());
        Assert.assertTrue("3rd interceptor did not get invoked.", intercept3.isReceivedACall());
        Assert.assertTrue("Tail interceptor did not get invoked.", tail.isReceivedACall());
    }

}
