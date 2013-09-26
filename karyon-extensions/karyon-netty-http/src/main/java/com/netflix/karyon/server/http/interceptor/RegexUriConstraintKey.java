package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides constraint implementation for {@link PipelineDefinition.Key} for matching URI paths as regular expressions as
 * supported by {@link Pattern}. <p/>
 * The request URI path is as retrieved using: {@link KeyEvaluationContext#getRequestUriPath(HttpRequest)}
 *
 * @author Nitesh Kant
 */
public class RegexUriConstraintKey implements PipelineDefinition.Key {

    private static final Logger logger = LoggerFactory.getLogger(RegexUriConstraintKey.class);

    private final Pattern regEx;

    public RegexUriConstraintKey(String constraint) {
        Preconditions.checkNotNull(constraint, "Constraint can not be null.");
        regEx = Pattern.compile(constraint);
    }

    @Override
    public boolean apply(FullHttpRequest request, KeyEvaluationContext context) {
        String requestUriPath = context.getRequestUriPath(request);
        boolean matches = false;
        if (null != requestUriPath) {
            Matcher matcher = regEx.matcher(requestUriPath);
            matches = matcher.matches();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Result for regex based uri constraint for uri path {} and pattern {} : {}",
                         new Object[] {requestUriPath, regEx, matches});
        }
        return matches;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegexUriConstraintKey{");
        sb.append("regEx=").append(regEx);
        sb.append('}');
        return sb.toString();
    }
}
