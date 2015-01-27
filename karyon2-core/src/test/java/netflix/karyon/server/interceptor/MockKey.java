package netflix.karyon.server.interceptor;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.interceptor.InterceptorKey;
import netflix.karyon.transport.interceptor.KeyEvaluationContext;

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
