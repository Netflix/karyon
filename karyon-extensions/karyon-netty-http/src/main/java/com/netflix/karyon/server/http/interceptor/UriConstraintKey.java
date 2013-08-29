package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.FullHttpRequest;

/**
* @author Nitesh Kant
*/
public class UriConstraintKey implements PipelineDefinition.Key {

    private final String constraint;

    public UriConstraintKey(String constraint) {
        this.constraint = constraint;
    }

    @Override
    public boolean apply(FullHttpRequest request) {
        return true;
    }
}
