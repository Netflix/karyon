package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
* @author Nitesh Kant
*/
public class TestableBidirectionalInterceptor extends BidirectionalInterceptorAdapter {

    private final PipelineDefinition.Key filterKey;
    private volatile boolean wasLastInCallValid;
    private volatile boolean wasLastOutCallValid;
    private volatile boolean calledForIn;
    private volatile boolean calledForOut;

    public TestableBidirectionalInterceptor(PipelineDefinition.Key filterKey) {
        this.filterKey = filterKey;
    }

    @Override
    protected void intercept(Direction direction, FullHttpRequest httpRequest,
                             HttpResponseWriter responseWriter, InterceptorExecutionContext executionContext) {
        switch (direction) {
            case INBOUND:
                wasLastInCallValid = filterKey.apply(httpRequest, new PipelineDefinition.Key.KeyEvaluationContext());
                calledForIn = true;
                break;
            case OUTBOUND:
                wasLastOutCallValid = filterKey.apply(httpRequest, new PipelineDefinition.Key.KeyEvaluationContext());
                calledForOut = true;
                break;
        }
        executionContext.executeNextInterceptor(httpRequest, responseWriter);
    }

    public boolean wasLastInCallValid() {
        return wasLastInCallValid;
    }

    public boolean wasLastOutCallValid() {
        return wasLastOutCallValid;
    }

    public boolean isCalledForIn() {
        return calledForIn;
    }

    public boolean isCalledForOut() {
        return calledForOut;
    }
}
