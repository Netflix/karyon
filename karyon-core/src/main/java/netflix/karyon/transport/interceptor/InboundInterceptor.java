package netflix.karyon.transport.interceptor;

import rx.Observable;

/**
 * @author Nitesh Kant
 */
public interface InboundInterceptor<I, O> {

    Observable<Void> in(I request, O response);
}
