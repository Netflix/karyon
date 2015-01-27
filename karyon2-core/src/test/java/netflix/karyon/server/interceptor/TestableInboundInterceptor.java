package netflix.karyon.server.interceptor;

import io.netty.buffer.ByteBuf;
import netflix.karyon.server.MockChannelHandlerContext;
import netflix.karyon.transport.interceptor.InboundInterceptor;
import netflix.karyon.transport.interceptor.InterceptorKey;
import netflix.karyon.transport.interceptor.KeyEvaluationContext;
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
