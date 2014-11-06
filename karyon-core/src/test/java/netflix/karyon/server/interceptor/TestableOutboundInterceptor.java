package netflix.karyon.server.interceptor;

import io.netty.buffer.ByteBuf;
import netflix.karyon.transport.interceptor.InterceptorKey;
import netflix.karyon.transport.interceptor.KeyEvaluationContext;

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
