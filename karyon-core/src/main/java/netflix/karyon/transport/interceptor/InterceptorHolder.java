package netflix.karyon.transport.interceptor;

import java.util.List;

/**
* @author Nitesh Kant
*/
public class InterceptorHolder<I, C extends KeyEvaluationContext, T> {

    private final InterceptorKey<I, C> key;
    private final List<T> interceptors;

    public InterceptorHolder(InterceptorKey<I, C> key, List<T> interceptors) {
        this.key = key;
        this.interceptors = interceptors;
    }

    public InterceptorKey<I, C> getKey() {
        return key;
    }

    public List<T> getInterceptors() {
        return interceptors;
    }
}
