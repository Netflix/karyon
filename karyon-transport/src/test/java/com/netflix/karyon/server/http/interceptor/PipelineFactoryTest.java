package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Nitesh Kant
 */
public class PipelineFactoryTest {

    @Test
    public void testInboundForUri() throws Exception {
        InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder = new InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse>();
        addInboundInterceptorForUriConstraint(builder, "/*");

        PipelineFactory<FullHttpRequest, FullHttpResponse> pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<InboundInterceptor<FullHttpRequest, FullHttpResponse>> in = pipelineFactory.getInboundInterceptors(request,
                                                                                                                null);
        Assert.assertEquals("Inbound interceptor not returned for matching URI.", 1, in.size());
    }

    @Test
    public void testOutboundForUri() throws Exception {
        InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder = new InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse>();
        addOutboundInterceptorForUriConstraint(builder, "/*");

        PipelineFactory<FullHttpRequest, FullHttpResponse> pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<OutboundInterceptor<FullHttpResponse>> out = pipelineFactory.getOutboundInterceptors(request, null);
        Assert.assertEquals("Outbound interceptor not returned for matching URI.", 1, out.size());
    }

    @Test
    public void testFilterOut() throws Exception {
        InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder = new InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse>();
        addOutboundInterceptorForUriConstraint(builder, "/abc/efg");
        addOutboundInterceptorForUriConstraint(builder, "/abc/*");

        addInboundInterceptorForUriConstraint(builder, "/abc/efg");
        addInboundInterceptorForUriConstraint(builder, "/abc/*");

        PipelineFactory<FullHttpRequest, FullHttpResponse> pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");

        List<OutboundInterceptor<FullHttpResponse>> out = pipelineFactory.getOutboundInterceptors(request, null);
        Assert.assertEquals("Outbound interceptor count not as expected.", 1, out.size());

        List<InboundInterceptor<FullHttpRequest, FullHttpResponse>> in = pipelineFactory.getInboundInterceptors(request, null);
        Assert.assertEquals("Inbound interceptor count not as expected.", 1, in.size());

        FullHttpRequest requestMatchingAll = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/efg");

        out = pipelineFactory.getOutboundInterceptors(requestMatchingAll, null);
        Assert.assertEquals("Outbound interceptor count not as expected.", 2, out.size());

        in = pipelineFactory.getInboundInterceptors(requestMatchingAll, null);
        Assert.assertEquals("Inbound interceptor count not as expected.", 2, in.size());

    }

    @Test
    public void testInboundForUriRegEx() throws Exception {
        InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder = new InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse>();
        addInboundInterceptorForUriRegExConstraint(builder, "/abc/.*");

        PipelineFactory<FullHttpRequest, FullHttpResponse> pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<InboundInterceptor<FullHttpRequest, FullHttpResponse>> in = pipelineFactory.getInboundInterceptors(request,
                                                                                                                null);
        Assert.assertEquals("Inbound interceptor not returned for matching regex.", 1, in.size());
    }

    @Test
    public void testOutboundForUriRegEx() throws Exception {
        InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder = new InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse>();
        addOutboundInterceptorForUriRegExConstraint(builder, "/abc/.*");

        PipelineFactory<FullHttpRequest, FullHttpResponse> pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<OutboundInterceptor<FullHttpResponse>> out = pipelineFactory.getOutboundInterceptors(request, null);
        Assert.assertEquals("Outbound interceptor not returned for matching regex.", 1, out.size());
    }

    @Test
    public void testInboundForHttpMethod() throws Exception {
        InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder = new InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse>();
        addInboundInterceptorForHttpMethod(builder, HttpMethod.DELETE);

        PipelineFactory<FullHttpRequest, FullHttpResponse> pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/abc/def");
        List<InboundInterceptor<FullHttpRequest, FullHttpResponse>> in = pipelineFactory.getInboundInterceptors(request,
                                                                                                                null);
        Assert.assertEquals("Inbound interceptor not returned for matching http method.", 1, in.size());
    }

    @Test
    public void testOutboundForHttpMethod() throws Exception {
        InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder = new InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse>();
        addOutboundInterceptorForHttpMethod(builder, HttpMethod.CONNECT);

        PipelineFactory<FullHttpRequest, FullHttpResponse> pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, "/abc/def");
        List<OutboundInterceptor<FullHttpResponse>> out = pipelineFactory.getOutboundInterceptors(request, null);
        Assert.assertEquals("Outbound interceptor not returned for matching regex.", 1, out.size());
    }

    private static TestableInboundInterceptor addInboundInterceptorForUriConstraint(InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder,
                                                                                    String constraint) {
        TestableInboundInterceptor interceptor = new TestableInboundInterceptor(new ServletStyleUriConstraintKey(constraint,
                                                                                                                 ""));
        builder.interceptIfUri(constraint, interceptor);
        return interceptor;
    }

    private static TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> addOutboundInterceptorForUriConstraint(
            InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder,
            String constraint) {
        TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> interceptor =
                new TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse>(new ServletStyleUriConstraintKey(constraint, ""));
        builder.interceptIfUri(constraint, (OutboundInterceptor<FullHttpResponse>) interceptor);
        return interceptor;
    }

    private static TestableInboundInterceptor addInboundInterceptorForUriRegExConstraint(InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder,
                                                                                    String constraint) {
        TestableInboundInterceptor interceptor = new TestableInboundInterceptor(new RegexUriConstraintKey(constraint));
        builder.interceptIfUriForRegex(constraint, interceptor);
        return interceptor;
    }

    private static TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> addOutboundInterceptorForUriRegExConstraint(
            InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder,
            String constraint) {
        TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> interceptor =
                new TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse>(new RegexUriConstraintKey(constraint));
        builder.interceptIfUriForRegex(constraint, (OutboundInterceptor<FullHttpResponse>) interceptor);
        return interceptor;
    }

    private static TestableInboundInterceptor addInboundInterceptorForHttpMethod(InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder,
                                                                                    HttpMethod method) {
        TestableInboundInterceptor interceptor = new TestableInboundInterceptor(new MethodConstraintKey(method));
        builder.interceptIfMethod(method, interceptor);
        return interceptor;
    }

    private static TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> addOutboundInterceptorForHttpMethod(InterceptorPipelineBuilder<FullHttpRequest, FullHttpResponse> builder,
            HttpMethod method) {
        TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse> interceptor = new TestableOutboundInterceptor<FullHttpRequest, FullHttpResponse>(new MethodConstraintKey(method));
        builder.interceptIfMethod(method, (OutboundInterceptor<FullHttpResponse>) interceptor);
        return interceptor;
    }
}
