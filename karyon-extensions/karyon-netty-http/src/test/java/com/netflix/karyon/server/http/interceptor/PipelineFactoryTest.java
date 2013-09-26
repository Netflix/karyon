package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
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
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addInboundInterceptorForUriConstraint(builder, "/*");

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<InboundInterceptor> in = pipelineFactory.getInboundInterceptors(request);
        Assert.assertEquals("Inbound interceptor not returned for matching URI.", 1, in.size());
    }

    @Test
    public void testOutboundForUri() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addOutboundInterceptorForUriConstraint(builder, "/*");

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<OutboundInterceptor> out = pipelineFactory.getOutboundInterceptors(request);
        Assert.assertEquals("Outbound interceptor not returned for matching URI.", 1, out.size());
    }

    @Test
    public void testFilterOut() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addOutboundInterceptorForUriConstraint(builder, "/abc/efg");
        addOutboundInterceptorForUriConstraint(builder, "/abc/*");

        addInboundInterceptorForUriConstraint(builder, "/abc/efg");
        addInboundInterceptorForUriConstraint(builder, "/abc/*");

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");

        List<OutboundInterceptor> out = pipelineFactory.getOutboundInterceptors(request);
        Assert.assertEquals("Outbound interceptor count not as expected.", 1, out.size());

        List<InboundInterceptor> in = pipelineFactory.getInboundInterceptors(request);
        Assert.assertEquals("Inbound interceptor count not as expected.", 1, in.size());

        FullHttpRequest requestMatchingAll = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/efg");

        out = pipelineFactory.getOutboundInterceptors(requestMatchingAll);
        Assert.assertEquals("Outbound interceptor count not as expected.", 2, out.size());

        in = pipelineFactory.getInboundInterceptors(requestMatchingAll);
        Assert.assertEquals("Inbound interceptor count not as expected.", 2, in.size());

    }

    @Test
    public void testBiDirectional() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addBidirectionalInterceptorForUriConstraint(builder, "abc/*");

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<InboundInterceptor> in = pipelineFactory.getInboundInterceptors(request);
        Assert.assertEquals("Bidirectional interceptor not returned as inbound.", 1, in.size());
        List<OutboundInterceptor> out = pipelineFactory.getOutboundInterceptors(request);
        Assert.assertEquals("Bidirectional interceptor not returned as outbound.", 1, out.size());
    }

    @Test
    public void testInboundForUriRegEx() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addInboundInterceptorForUriRegExConstraint(builder, "/abc/.*");

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<InboundInterceptor> in = pipelineFactory.getInboundInterceptors(request);
        Assert.assertEquals("Inbound interceptor not returned for matching regex.", 1, in.size());
    }

    @Test
    public void testOutboundForUriRegEx() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addOutboundInterceptorForUriRegExConstraint(builder, "/abc/.*");

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<OutboundInterceptor> out = pipelineFactory.getOutboundInterceptors(request);
        Assert.assertEquals("Outbound interceptor not returned for matching regex.", 1, out.size());
    }

    @Test
    public void testBiDirectionalForUriRegEx() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addBidirectionalInterceptorForUriRegexConstraint(builder, "/abc/.*");

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<InboundInterceptor> in = pipelineFactory.getInboundInterceptors(request);
        Assert.assertEquals("Bidirectional interceptor not returned as inbound.", 1, in.size());
        List<OutboundInterceptor> out = pipelineFactory.getOutboundInterceptors(request);
        Assert.assertEquals("Bidirectional interceptor not returned as outbound.", 1, out.size());
    }

    @Test
    public void testInboundForHttpMethod() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addInboundInterceptorForHttpMethod(builder, HttpMethod.DELETE);

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/abc/def");
        List<InboundInterceptor> in = pipelineFactory.getInboundInterceptors(request);
        Assert.assertEquals("Inbound interceptor not returned for matching http method.", 1, in.size());
    }

    @Test
    public void testOutboundForHttpMethod() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addOutboundInterceptorForHttpMethod(builder, HttpMethod.CONNECT);

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, "/abc/def");
        List<OutboundInterceptor> out = pipelineFactory.getOutboundInterceptors(request);
        Assert.assertEquals("Outbound interceptor not returned for matching regex.", 1, out.size());
    }

    @Test
    public void testBiDirectionalForHttpMethod() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addBidirectionalInterceptorForHttpMethod(builder, HttpMethod.POST);

        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/abc/def");
        List<InboundInterceptor> in = pipelineFactory.getInboundInterceptors(request);
        Assert.assertEquals("Bidirectional interceptor not returned as inbound.", 1, in.size());
        List<OutboundInterceptor> out = pipelineFactory.getOutboundInterceptors(request);
        Assert.assertEquals("Bidirectional interceptor not returned as outbound.", 1, out.size());
    }

    private static TestableBidirectionalInterceptor addBidirectionalInterceptorForUriConstraint(InterceptorPipelineBuilder builder,
                                                                                                String constraint) {
        TestableBidirectionalInterceptor interceptor = new TestableBidirectionalInterceptor(new ServletStyleUriConstraintKey(constraint));
        builder.interceptIfUri(constraint, interceptor);
        return interceptor;
    }

    private static TestableBidirectionalInterceptor addBidirectionalInterceptorForUriRegexConstraint(InterceptorPipelineBuilder builder,
                                                                                                     String constraint) {
        TestableBidirectionalInterceptor interceptor = new TestableBidirectionalInterceptor(new RegexUriConstraintKey(constraint));
        builder.interceptIfUriForRegex(constraint, interceptor);
        return interceptor;
    }

    private static TestableInboundInterceptor addInboundInterceptorForUriConstraint(InterceptorPipelineBuilder builder,
                                                                                    String constraint) {
        TestableInboundInterceptor interceptor = new TestableInboundInterceptor(new ServletStyleUriConstraintKey(constraint));
        builder.interceptIfUri(constraint, interceptor);
        return interceptor;
    }

    private static TestableOutboundInterceptor addOutboundInterceptorForUriConstraint(
            InterceptorPipelineBuilder builder,
            String constraint) {
        TestableOutboundInterceptor interceptor = new TestableOutboundInterceptor(new ServletStyleUriConstraintKey(constraint));
        builder.interceptIfUri(constraint, interceptor);
        return interceptor;
    }

    private static TestableInboundInterceptor addInboundInterceptorForUriRegExConstraint(InterceptorPipelineBuilder builder,
                                                                                    String constraint) {
        TestableInboundInterceptor interceptor = new TestableInboundInterceptor(new RegexUriConstraintKey(constraint));
        builder.interceptIfUriForRegex(constraint, interceptor);
        return interceptor;
    }

    private static TestableOutboundInterceptor addOutboundInterceptorForUriRegExConstraint(
            InterceptorPipelineBuilder builder,
            String constraint) {
        TestableOutboundInterceptor interceptor = new TestableOutboundInterceptor(new RegexUriConstraintKey(constraint));
        builder.interceptIfUriForRegex(constraint, interceptor);
        return interceptor;
    }

    private static TestableInboundInterceptor addInboundInterceptorForHttpMethod(InterceptorPipelineBuilder builder,
                                                                                    HttpMethod method) {
        TestableInboundInterceptor interceptor = new TestableInboundInterceptor(new MethodConstraintKey(method));
        builder.interceptIfMethod(method, interceptor);
        return interceptor;
    }

    private static TestableOutboundInterceptor addOutboundInterceptorForHttpMethod(InterceptorPipelineBuilder builder,
            HttpMethod method) {
        TestableOutboundInterceptor interceptor = new TestableOutboundInterceptor(new MethodConstraintKey(method));
        builder.interceptIfMethod(method, interceptor);
        return interceptor;
    }


    private static TestableBidirectionalInterceptor addBidirectionalInterceptorForHttpMethod(InterceptorPipelineBuilder builder,
                                                                                             HttpMethod method) {
        TestableBidirectionalInterceptor interceptor = new TestableBidirectionalInterceptor(new MethodConstraintKey(method));
        builder.interceptIfMethod(method, interceptor);
        return interceptor;
    }

}
