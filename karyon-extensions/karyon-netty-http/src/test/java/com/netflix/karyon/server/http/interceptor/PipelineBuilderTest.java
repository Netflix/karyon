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
public class PipelineBuilderTest {

    @Test
    public void testInboundForUri() throws Exception {
        InterceptorPipelineBuilder builder = new InterceptorPipelineBuilder();
        addInboundInterceptorForConstratint(builder, "/*");
        PipelineFactory pipelineFactory = builder.buildFactory();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/abc/def");
        List<InboundInterceptor> in = pipelineFactory.getInboundInterceptors(request);
        Assert.assertEquals(1, in.size());
    }

    private static TestableInboundInterceptor addInboundInterceptorForConstratint(InterceptorPipelineBuilder builder,
                                                                                  String constraint) {
        TestableInboundInterceptor interceptor = new TestableInboundInterceptor(new ServletStyleUriConstraintKey(constraint));
        builder.interceptIfUri(constraint, interceptor);
        return interceptor;
    }
}
