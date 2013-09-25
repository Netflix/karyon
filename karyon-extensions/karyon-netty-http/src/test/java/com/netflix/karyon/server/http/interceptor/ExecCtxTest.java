package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
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
        List<InboundInterceptor> interceptors = new ArrayList<InboundInterceptor>();
        TestableInboundInterceptor intercept1 = new TestableInboundInterceptor(new ServletStyleUriConstraintKey("*"));
        TestableInboundInterceptor intercept2 = new TestableInboundInterceptor(new ServletStyleUriConstraintKey("*"));
        TestableInboundInterceptor intercept3 = new TestableInboundInterceptor(new ServletStyleUriConstraintKey("*"));
        TestableInboundInterceptor tail = new TestableInboundInterceptor(new ServletStyleUriConstraintKey("*"));
        interceptors.add(intercept1);
        interceptors.add(intercept2);
        interceptors.add(intercept3);

        InterceptorExecutionContextImpl ctx = new InterceptorExecutionContextImpl(interceptors, tail);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def"); // contents do not matter as we do not run filtering
        ctx.executeNextInterceptor(request, new HttpResponseWriterMock());


        Assert.assertTrue("1st interceptor did not get invoked.", intercept1.isReceivedACall());
        Assert.assertTrue("2rd interceptor did not get invoked.", intercept2.isReceivedACall());
        Assert.assertTrue("3rd interceptor did not get invoked.", intercept3.isReceivedACall());
        Assert.assertTrue("Tail interceptor did not get invoked.", tail.isReceivedACall());
    }

    @Test
    public void testOutboundCtx() throws Exception {
        List<OutboundInterceptor> interceptors = new ArrayList<OutboundInterceptor>();
        TestableOutboundInterceptor intercept1 = new TestableOutboundInterceptor(new ServletStyleUriConstraintKey("*"));
        TestableOutboundInterceptor intercept2 = new TestableOutboundInterceptor(new ServletStyleUriConstraintKey("*"));
        TestableOutboundInterceptor intercept3 = new TestableOutboundInterceptor(new ServletStyleUriConstraintKey("*"));
        TestableOutboundInterceptor tail = new TestableOutboundInterceptor(new ServletStyleUriConstraintKey("*"));
        interceptors.add(intercept1);
        interceptors.add(intercept2);
        interceptors.add(intercept3);

        InterceptorExecutionContextImpl ctx = new InterceptorExecutionContextImpl(interceptors, tail);
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def"); // contents do not matter as we do not run filtering
        ctx.executeNextInterceptor(request, new HttpResponseWriterMock());


        Assert.assertTrue("1st interceptor did not get invoked.", intercept1.isReceivedACall());
        Assert.assertTrue("2rd interceptor did not get invoked.", intercept2.isReceivedACall());
        Assert.assertTrue("3rd interceptor did not get invoked.", intercept3.isReceivedACall());
        Assert.assertTrue("Tail interceptor did not get invoked.", tail.isReceivedACall());
    }

}
