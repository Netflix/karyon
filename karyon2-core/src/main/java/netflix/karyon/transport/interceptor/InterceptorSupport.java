package netflix.karyon.transport.interceptor;

/**
 * A contract to support interceptors for requests i.e. how to attach filters to a request processing.
 *
 * @author Nitesh Kant
 */
public class InterceptorSupport<I, O, C extends KeyEvaluationContext>
        extends AbstractInterceptorSupport<I, O, C, InterceptorSupport.Attacher<I, O, C>, InterceptorSupport<I, O, C>> {

    @Override
    protected Attacher<I, O, C> newAttacher(InterceptorKey<I, C> key) {
        return new Attacher<I, O, C>(this, key);
    }

    public static class Attacher<I, O, C extends KeyEvaluationContext>
            extends AbstractAttacher<I, O, C, InterceptorSupport<I, O, C>> {

        public Attacher(InterceptorSupport<I, O, C> interceptorSupport, InterceptorKey<I, C> key) {
            super(interceptorSupport, key);
        }
    }
}
