package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
* @author Nitesh Kant
*/
class TestableInboundInterceptor implements InboundInterceptor<FullHttpRequest, FullHttpResponse> {

    private final PipelineDefinition.Key filterKey;
    private volatile boolean wasLastCallValid;
    private volatile boolean receivedACall;

    public TestableInboundInterceptor(PipelineDefinition.Key filterKey) {
        this.filterKey = filterKey;
    }

    @Override
    public void interceptIn(FullHttpRequest httpRequest, ResponseWriter<FullHttpResponse> responseWriter,
                            NextInterceptorInvoker<FullHttpRequest, FullHttpResponse> executionContext) {
        wasLastCallValid = filterKey.apply(httpRequest, new PipelineDefinition.Key.KeyEvaluationContext());
        receivedACall = true;
        executionContext.executeNext(httpRequest, responseWriter);
    }

    public boolean wasLastCallValid() {
        return wasLastCallValid;
    }

    boolean isReceivedACall() {
        return receivedACall;
    }
}
