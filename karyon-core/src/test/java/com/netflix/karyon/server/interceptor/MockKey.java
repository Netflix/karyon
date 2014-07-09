package com.netflix.karyon.server.interceptor;

import com.netflix.karyon.transport.interceptor.InterceptorKey;
import com.netflix.karyon.transport.interceptor.KeyEvaluationContext;
import io.netty.buffer.ByteBuf;

/**
* @author Nitesh Kant
*/
class MockKey implements InterceptorKey<ByteBuf, KeyEvaluationContext> {

    private final boolean result;

    public MockKey(boolean result) {
        this.result = result;
    }

    @Override
    public boolean apply(ByteBuf request, KeyEvaluationContext context) {
        return result;
    }
}
