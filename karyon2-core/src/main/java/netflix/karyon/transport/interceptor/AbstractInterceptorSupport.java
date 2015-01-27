package netflix.karyon.transport.interceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Nitesh Kant
 */
public abstract class AbstractInterceptorSupport<I, O, C extends KeyEvaluationContext,
                                                 A extends AbstractAttacher<I, O, C, S>,
                                                 S extends AbstractInterceptorSupport<I, O, C, A, S>> {

    protected final Map<InterceptorKey<I, C>, A> attachers;
    protected List<InterceptorHolder<I, C, InboundInterceptor<I, O>>> inboundInterceptors;
    protected List<InterceptorHolder<I, C, OutboundInterceptor<O>>> outboundInterceptors;
    protected boolean finished;

    public AbstractInterceptorSupport() {
        attachers = new HashMap<InterceptorKey<I, C>, A>();
        inboundInterceptors = new LinkedList<InterceptorHolder<I, C, InboundInterceptor<I, O>>>();
        outboundInterceptors = new LinkedList<InterceptorHolder<I, C, OutboundInterceptor<O>>>();
    }

    protected List<InterceptorHolder<I, C, InboundInterceptor<I, O>>> getInboundInterceptors() {
        return inboundInterceptors;
    }

    protected List<InterceptorHolder<I, C, OutboundInterceptor<O>>> getOutboundInterceptors() {
        return outboundInterceptors;
    }

    public A forKey(InterceptorKey<I, C> key) {
        if (finished) {
            throw new IllegalArgumentException("Interceptor support can not be modified after finishing.");
        }
        return getAttacherForKey(key);
    }

    protected A getAttacherForKey(InterceptorKey<I, C> key) {
        A attacher = attachers.get(key);
        if (null == attacher) {
            attacher = newAttacher(key);
            attachers.put(key, attacher);
        }
        return attacher;
    }

    protected abstract A newAttacher(InterceptorKey<I, C> key);

    public boolean hasAtleastOneInterceptor() {
        return !inboundInterceptors.isEmpty() || !outboundInterceptors.isEmpty();
    }

    public S finish() {
        if (!finished) {
            _finish();
            finished = true;
        }
        return returnSupport();
    }

    protected void _finish() {
        attachers.clear();
        inboundInterceptors = Collections.unmodifiableList(inboundInterceptors);
        outboundInterceptors = Collections.unmodifiableList(outboundInterceptors);
    }

    @SuppressWarnings("unchecked")
    protected S returnSupport() {
        return (S) this;
    }
}
