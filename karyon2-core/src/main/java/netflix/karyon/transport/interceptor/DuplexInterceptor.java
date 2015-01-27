package netflix.karyon.transport.interceptor;

/**
 *
 * @author Nitesh Kant
 */
public interface DuplexInterceptor<I, O> extends InboundInterceptor<I, O>, OutboundInterceptor<O> {
}
