package netflix.karyon.transport.interceptor;

/**
 * Key for a {@link InboundInterceptor} or {@link OutboundInterceptor} which determines whether a interceptor must
 * be applied for a particular request. <br>
 * Any implementation for the key must be aware that it will be invoked for every request so it should always optimize
 * for speed of evaluation.
 *
 * @author Nitesh Kant
 */
public interface InterceptorKey<I, C extends KeyEvaluationContext> {

    /**
     * This is invoked for a request to determine whether the interceptor attached to this key must be executed for this
     * request.
     *
     * @param request Request to determine whether the attached interceptor is to be applied or not.
     * @param context Context for the key evaluation, usually used to cache costly operations like parsing request
     *                URI.
     *
     * @return {@code true} if the interceptor must be applied for this request.
     */
    boolean apply(I request, C context);

}
