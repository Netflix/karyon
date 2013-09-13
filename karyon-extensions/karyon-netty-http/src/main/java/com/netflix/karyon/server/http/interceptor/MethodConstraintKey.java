package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

/**
* @author Nitesh Kant
*/
public class MethodConstraintKey implements PipelineDefinition.Key {

    private final HttpMethod method;

    public MethodConstraintKey(HttpMethod method) {
        Preconditions.checkNotNull(method, "HTTP method in the interceptor constraint can not be null.");
        this.method = method;
    }

    @Override
    public boolean apply(FullHttpRequest request, KeyEvaluationContext context) {
        return request.getMethod().equals(method);
    }
}
