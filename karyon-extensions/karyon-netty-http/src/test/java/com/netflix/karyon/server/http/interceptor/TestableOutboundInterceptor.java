package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.HttpObject;

/**
* @author Nitesh Kant
*/
public class TestableOutboundInterceptor<I extends HttpObject, O extends HttpObject>
        extends TestableBidirectionalInterceptor<I, O> {

    public TestableOutboundInterceptor(PipelineDefinition.Key filterKey) {
        super(filterKey, null);
    }

    public boolean isReceivedACall() {
        return isCalledForOut();
    }
}
