package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

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

    private final Pattern regEx;

    public RegexUriConstraintKey(String constraint) {
        Preconditions.checkNotNull(constraint, "Constraint can not be null.");
        regEx = Pattern.compile(constraint);
    }

    @Override
    public boolean apply(FullHttpRequest request, KeyEvaluationContext context) {
        String requestUriPath = context.getRequestUriPath(request);
        if (null != requestUriPath) {
            Matcher matcher = regEx.matcher(requestUriPath);
            return matcher.matches();
        }
        return false;
    }
}
