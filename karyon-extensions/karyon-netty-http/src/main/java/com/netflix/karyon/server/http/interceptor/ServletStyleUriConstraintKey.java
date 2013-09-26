package com.netflix.karyon.server.http.interceptor;

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Provides constraint implementation for {@link PipelineDefinition.Key} similar to java servlet specifications. <p/>
 * The following types of constraints are supported:
 * <ul>
 <li>Exact uri mapping: Give the exact string that should match the URI path of the incoming request.
 eg: "/myresource/foo" will match <em>ALL</em> the URIs:
 <ul>
     <li>"/myresource/foo"</li>
     <li>"/myresource/foo/"</li>
 </ul>
 <li> Prefix uri mapping: Give the prefix in the URI path you want to match, ending with a "*". eg: "/myresource/foo/*" will
 match <em>ALL</em> the URIs:
 <ul>
        <li>"/myresource/foo/bar"</li>
        <li>"/myresource/foo/"</li>
        <li>"/myresource/foo"</li>
 </ul>
 </li>
 </ul>
 *
 * In any of the pattersn above the leading slash in the constraint is optional, i.e., "/myresource/foo" is equivalent to
 * "myresource/foo". <p/>
 *
 * As compared to servlets/web applications there are no context paths of an application so the URI path has to be
 * absolute. <br/>
 *
 * The request URI path is as retrieved using: {@link KeyEvaluationContext#getRequestUriPath(HttpRequest)}
 *
 * @author Nitesh Kant
 */
public class ServletStyleUriConstraintKey implements PipelineDefinition.Key {

    private static final Logger logger = LoggerFactory.getLogger(ServletStyleUriConstraintKey.class);

    private final Matcher matcher;

    public ServletStyleUriConstraintKey(String constraint) {
        Preconditions.checkNotNull(constraint, "Constraint can not be null.");
        if (!constraint.startsWith("/")) { // URI always comes with a leading '/'
            constraint = '/' + constraint;
        }

        if (constraint.endsWith("/*")) {
            matcher = new PrefixMatcher(constraint.substring(0, constraint.length() - 1),
                                        new Matcher(constraint.substring(0, constraint.length() - 2), null)); // Prefix match removing * or exact match removing /*
        } else if (constraint.endsWith("*")) {
            matcher = new PrefixMatcher(constraint.substring(0, constraint.length() - 1), null);
        } else {
            matcher = new Matcher(constraint, new Matcher(constraint + '/', null));
        }
    }

    @Override
    public boolean apply(FullHttpRequest request, KeyEvaluationContext context) {
        String requestUriPath = context.getRequestUriPath(request);
        boolean matches = false;
        if (null != requestUriPath) {
            matches = matcher.match(requestUriPath);
        }
        return matches;
    }

    private static class Matcher {

        protected final String constraint;

        @Nullable
        private final Matcher nextMatcher;

        private Matcher(String constraint, @Nullable Matcher nextMatcher) {
            this.constraint = constraint;
            this.nextMatcher = nextMatcher;
        }

        protected boolean match(String requestUriPath) {
            return isMatching(requestUriPath) || null != nextMatcher && nextMatcher.match(requestUriPath);
        }

        protected boolean isMatching(String requestUriPath) {
            boolean matches = requestUriPath.equals(constraint);
            if (logger.isDebugEnabled()) {
                logger.debug("Exact match result for servlet style uri constraint for uri path {} and constraint {} : {}",
                             new Object[] {requestUriPath, constraint, matches});
            }
            return matches;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Matcher{");
            sb.append("constraint='").append(constraint).append('\'');
            sb.append(", nextMatcher=").append(nextMatcher);
            sb.append('}');
            return sb.toString();
        }
    }

    private static class PrefixMatcher extends Matcher {

        private PrefixMatcher(String prefix, @Nullable Matcher nextMatcher) {
            super(prefix, nextMatcher);
        }

        @Override
        protected boolean isMatching(String requestUriPath) {
            boolean matches = requestUriPath.startsWith(constraint);
            if (logger.isDebugEnabled()) {
                logger.debug("Prefix match result for servlet style uri constraint for uri path {} and constraint {} : {}",
                             new Object[] {requestUriPath, constraint, matches});
            }
            return matches;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServletStyleUriConstraintKey{");
        sb.append("matcher=").append(matcher);
        sb.append('}');
        return sb.toString();
    }
}
