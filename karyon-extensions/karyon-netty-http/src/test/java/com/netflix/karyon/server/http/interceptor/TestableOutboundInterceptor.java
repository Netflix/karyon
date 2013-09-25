package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
* @author Nitesh Kant
*/
public class TestableOutboundInterceptor implements OutboundInterceptor {

    private final PipelineDefinition.Key filterKey;
    private volatile boolean wasLastCallValid;
    private volatile boolean receivedACall;

    public TestableOutboundInterceptor(PipelineDefinition.Key filterKey) {
        this.filterKey = filterKey;
    }

    public boolean wasLastCallValid() {
        return wasLastCallValid;
    }

    @Override
    public void interceptOut(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                             InterceptorExecutionContext executionContext) {
        wasLastCallValid = filterKey.apply(httpRequest, new PipelineDefinition.Key.KeyEvaluationContext());
        receivedACall = true;
        executionContext.executeNextInterceptor(httpRequest, responseWriter);
    }

    public boolean isWasLastCallValid() {
        return wasLastCallValid;
    }

    public boolean isReceivedACall() {
        return receivedACall;
    }
}
