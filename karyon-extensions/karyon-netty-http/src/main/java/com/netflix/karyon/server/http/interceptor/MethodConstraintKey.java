package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author Nitesh Kant
*/
public class MethodConstraintKey implements PipelineDefinition.Key {

    private static final Logger logger = LoggerFactory.getLogger(MethodConstraintKey.class);

    private final HttpMethod method;

    public MethodConstraintKey(HttpMethod method) {
        Preconditions.checkNotNull(method, "HTTP method in the interceptor constraint can not be null.");
        this.method = method;
    }

    @Override
    public boolean apply(FullHttpRequest request, KeyEvaluationContext context) {
        boolean matches = request.getMethod().equals(method);
        if (logger.isDebugEnabled()) {
            logger.debug("Result for HTTP method constraint for method {} and required method {} : {}",
                         new Object[] {request.getMethod(), method, matches});
        }

        return matches;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MethodConstraintKey{");
        sb.append("method=").append(method);
        sb.append('}');
        return sb.toString();
    }
}
