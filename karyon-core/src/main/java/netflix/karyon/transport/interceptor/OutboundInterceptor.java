package netflix.karyon.transport.interceptor;

import rx.Observable;

/**
 * @author Nitesh Kant
 */
public interface OutboundInterceptor<O> {

    Observable<Void> out(O response);
}
