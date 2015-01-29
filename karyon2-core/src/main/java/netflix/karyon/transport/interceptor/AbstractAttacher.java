package netflix.karyon.transport.interceptor;

import java.util.Arrays;
import java.util.List;

/**
 * @author Nitesh Kant
 */
public class AbstractAttacher<I, O, C extends KeyEvaluationContext, S extends AbstractInterceptorSupport> {

    protected final S interceptorSupport;
    protected final InterceptorKey<I, C> key;

    public AbstractAttacher(S interceptorSupport, InterceptorKey<I, C> key) {
        this.interceptorSupport = interceptorSupport;
        this.key = key;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public S intercept(InboundInterceptor<I, O>... interceptors) {
        List list = interceptorSupport.getInboundInterceptors();
        list.add(new InterceptorHolder<I, C, InboundInterceptor<I, O>>(key, Arrays.asList(interceptors)));
        return interceptorSupport;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public S intercept(OutboundInterceptor<O>... interceptors) {
        List list = interceptorSupport.getOutboundInterceptors();
        list.add(new InterceptorHolder<I, C, OutboundInterceptor<O>>(key, Arrays.asList(interceptors)));
        return interceptorSupport;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public S intercept(DuplexInterceptor<I, O>... interceptors) {
        List ins = interceptorSupport.getInboundInterceptors();
        ins.add(new InterceptorHolder(key, Arrays.asList(interceptors)));
        List outs = interceptorSupport.getOutboundInterceptors();
        outs.add(new InterceptorHolder(key, Arrays.asList(interceptors)));
        return interceptorSupport;
    }

    public InterceptorKey<I, C> getKey() {
        return key;
    }
}
