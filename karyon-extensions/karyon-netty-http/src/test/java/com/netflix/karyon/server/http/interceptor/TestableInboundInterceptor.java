package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
* @author Nitesh Kant
*/
class TestableInboundInterceptor implements InboundInterceptor {

    private final PipelineDefinition.Key filterKey;
    private volatile boolean wasLastCallValid;
    private volatile boolean receivedACall;

    public TestableInboundInterceptor(PipelineDefinition.Key filterKey) {
        this.filterKey = filterKey;
    }

    @Override
    public void interceptIn(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                            InterceptorExecutionContext executionContext) {
        wasLastCallValid = filterKey.apply(httpRequest, new PipelineDefinition.Key.KeyEvaluationContext());
        receivedACall = true;
        executionContext.executeNextInterceptor(httpRequest, responseWriter);
    }

    public boolean wasLastCallValid() {
        return wasLastCallValid;
    }

    boolean isReceivedACall() {
        return receivedACall;
    }
}
