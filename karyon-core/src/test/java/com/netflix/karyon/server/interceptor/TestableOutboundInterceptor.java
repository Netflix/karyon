package com.netflix.karyon.server.interceptor;

import com.netflix.karyon.transport.interceptor.InterceptorKey;
import com.netflix.karyon.transport.interceptor.KeyEvaluationContext;
import io.netty.buffer.ByteBuf;

/**
* @author Nitesh Kant
*/
public class TestableOutboundInterceptor extends TestableDuplexInterceptor {

    public TestableOutboundInterceptor(InterceptorKey<ByteBuf, KeyEvaluationContext> filterKey) {
        super(filterKey);
    }

    public boolean isReceivedACall() {
        return isCalledForOut();
    }
}
