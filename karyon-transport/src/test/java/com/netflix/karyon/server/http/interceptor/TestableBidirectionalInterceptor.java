package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.HttpServerBuilder;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import javax.annotation.Nullable;

/**
 * @author Nitesh Kant
 */
public class TestableBidirectionalInterceptor<I extends HttpObject, O extends HttpObject>
        implements InboundInterceptor<I, O>, OutboundInterceptor<O> {

    private final PipelineDefinition.Key constraintKey;
    private volatile boolean inCalled;
    private volatile boolean outCalled;
    private volatile boolean wasLastInCallValid;
    private volatile boolean wasLastOutCallValid;
    private HttpRequest lastInCallRequest;

    @SuppressWarnings("unchecked")
    public TestableBidirectionalInterceptor(PipelineDefinition.Key constraintKey,
                                            @Nullable @SuppressWarnings("rawtypes") HttpServerBuilder.InterceptorAttacher attacher) {
        this.constraintKey = constraintKey;
        if (null != attacher) {
            attacher.interceptWith((InboundInterceptor<I, O>)this);
            attacher.interceptWith((OutboundInterceptor<O>)this);
        }
    }

    @Override
    public void interceptIn(I httpRequest, ResponseWriter<O> responseWriter, NextInterceptorInvoker<I, O> invoker) {
        inCalled = true;
        if (HttpRequest.class.isAssignableFrom(httpRequest.getClass())) {
            lastInCallRequest = (HttpRequest) httpRequest;
            wasLastInCallValid = constraintKey.apply((HttpRequest) httpRequest, new PipelineDefinition.Key.KeyEvaluationContext());
        }
        invoker.executeNext(httpRequest, responseWriter);
    }

    @Override
    public void interceptOut(O httpResponse, ResponseWriter<O> responseWriter, NextInterceptorInvoker<O, O> invoker) {
        outCalled = true;
        wasLastOutCallValid = constraintKey.apply(lastInCallRequest, new PipelineDefinition.Key.KeyEvaluationContext());
        invoker.executeNext(httpResponse, responseWriter);
    }

    public void setLastInCallRequest(HttpRequest lastInCallRequest) {
        this.lastInCallRequest = lastInCallRequest;
    }

    public boolean isCalledForIn() {
        return inCalled;
    }

    public boolean isCalledForOut() {
        return outCalled;
    }

    public boolean wasLastInCallValid() {
        return wasLastInCallValid;
    }

    public boolean wasLastOutCallValid() {
        return wasLastOutCallValid;
    }
}
