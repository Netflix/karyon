package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @author Nitesh Kant
 */
public class InterceptorConstraintTestBase {

    protected static boolean doApplyForGET(PipelineDefinition.Key key, String uri) {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        return key.apply(request, new PipelineDefinition.Key.KeyEvaluationContext());
    }

    protected static boolean doApply(PipelineDefinition.Key key, String uri, HttpMethod httpMethod) {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, uri);
        return key.apply(request, new PipelineDefinition.Key.KeyEvaluationContext());
    }
}
