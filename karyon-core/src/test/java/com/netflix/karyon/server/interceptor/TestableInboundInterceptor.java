package com.netflix.karyon.server.interceptor;

import com.netflix.karyon.server.MockChannelHandlerContext;
import com.netflix.karyon.transport.interceptor.InboundInterceptor;
import com.netflix.karyon.transport.interceptor.InterceptorKey;
import com.netflix.karyon.transport.interceptor.KeyEvaluationContext;
import io.netty.buffer.ByteBuf;
import rx.Observable;

/**
* @author Nitesh Kant
*/
class TestableInboundInterceptor implements InboundInterceptor<ByteBuf, ByteBuf> {

    private final InterceptorKey<ByteBuf, KeyEvaluationContext> filterKey;
    private volatile boolean wasLastCallValid;
    private volatile boolean receivedACall;

    public TestableInboundInterceptor(InterceptorKey<ByteBuf, KeyEvaluationContext> filterKey) {
        this.filterKey = filterKey;
    }

    public boolean wasLastCallValid() {
        return wasLastCallValid;
    }

    boolean isReceivedACall() {
        return receivedACall;
    }

    @Override
    public Observable<Void> in(ByteBuf request, ByteBuf response) {
        MockChannelHandlerContext context = new MockChannelHandlerContext("mock");
        wasLastCallValid = filterKey.apply(request, new KeyEvaluationContext(context.channel()));
        receivedACall = true;
        return Observable.empty();
    }
}
