package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpResponseWriter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
* @author Nitesh Kant
*/
class TestableInboundInterceptor implements InboundInterceptor {

    private final PipelineDefinition.Key filterKey;
    private volatile boolean wasLastCallValid;

    public TestableInboundInterceptor(PipelineDefinition.Key filterKey) {
        this.filterKey = filterKey;
    }

    @Override
    public void interceptIn(FullHttpRequest httpRequest, HttpResponseWriter responseWriter,
                            InterceptorExecutionContext executionContext) {
        wasLastCallValid = filterKey.apply(httpRequest, null);
    }

    public boolean wasLastCallValid() {
        return wasLastCallValid;
    }
}
