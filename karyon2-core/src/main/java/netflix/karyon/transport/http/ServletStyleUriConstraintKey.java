package netflix.karyon.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides constraint implementation for {@link netflix.karyon.transport.interceptor.InterceptorKey} similar to java servlet specifications. <p></p>
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
 * "myresource/foo".
 *
 * As compared to servlets/web applications there are no context paths of an application so the URI path has to be
 * absolute. <br>
 *
 * The request URI path is as retrieved using: {@link HttpKeyEvaluationContext#getRequestUriPath(HttpServerRequest)}
 *
 * @author Nitesh Kant
 */
public class ServletStyleUriConstraintKey<I> implements HttpInterceptorKey<I> {

    private static final Logger logger = LoggerFactory.getLogger(ServletStyleUriConstraintKey.class);

    private final Matcher matcher;
    private final String contextPath;

    public ServletStyleUriConstraintKey(final String constraint, final String contextPath) {
        if (null == constraint) {
            throw new NullPointerException("Constraint can not be null.");
        }
        this.contextPath = contextPath.startsWith("/") ? contextPath
                                                       : '/' + contextPath; // uri & constraint always starts with a /
        String normalizedConstraint = constraint;
        if (!constraint.startsWith("/")) { // URI always comes with a leading '/'
            normalizedConstraint = '/' + constraint;
        }

        if (normalizedConstraint.startsWith("/*.")) {
            matcher = new ExtensionMatcher(
                    constraint); // not normalizedConstraint as then we will have to ignore the first character.
        } else if (normalizedConstraint.endsWith("/*")) {
            matcher = new PrefixMatcher(normalizedConstraint.substring(0, normalizedConstraint.length() - 1),
                                        new Matcher(normalizedConstraint.substring(0,
                                                                                   normalizedConstraint.length() - 2),
                                                    null)); // Prefix match removing * or exact match removing /*
        } else if (normalizedConstraint.endsWith("*")) {
            matcher = new PrefixMatcher(normalizedConstraint.substring(0, normalizedConstraint.length() - 1), null);
        } else {
            matcher = new Matcher(normalizedConstraint, new Matcher(normalizedConstraint + '/', null));
        }
    }

    /**
     * This must be called if and only if {@link #apply(HttpServerRequest, HttpKeyEvaluationContext)}
     * returned for the same request.
     *
     * @param request Request which satisfies this key.
     * @param context The key evaluation context.
     *
     * @return The servlet path.
     */
    public String getServletPath(HttpServerRequest<I> request, HttpKeyEvaluationContext context) {
        String requestUriPath = context.getRequestUriPath(request);
        if (null != requestUriPath) {
            return matcher.getServletPath(requestUriPath);
        }
        return "";
    }

    @Override
    public boolean apply(HttpServerRequest<I> request, HttpKeyEvaluationContext context) {
        String requestUriPath = context.getRequestUriPath(request);
        boolean matches = false;
        if (null != requestUriPath) {
            matches = matcher.match(requestUriPath);
        }
        return matches;
    }

    private class Matcher {

        protected final String constraint;
        protected final String constraintWithoutContextPath;

        private final Matcher nextMatcher;

        private Matcher(String constraint, Matcher nextMatcher) {
            this.constraint = constraint;
            constraintWithoutContextPath = constraint.isEmpty() ? constraint : constraint.substring(contextPath.length());
            this.nextMatcher = nextMatcher;
        }

        protected boolean match(String requestUriPath) {
            return isMatching(requestUriPath, false) || null != nextMatcher && nextMatcher.match(requestUriPath);
        }

        protected boolean isMatching(String requestUriPath, boolean noLog) {
            boolean matches = requestUriPath.equals(constraint);
            if (!noLog && logger.isDebugEnabled()) {
                logger.debug("Exact match result for servlet style uri constraint for uri path {} and constraint {} : {}",
                             requestUriPath, constraint, matches);
            }
            return matches;
        }

        public String getServletPath(String requestUriPath) {
            if (requestUriPath.equals(constraint)) {
                // exact match & hence not required to query the next matcher.
                return constraintWithoutContextPath;
            }
            return null != nextMatcher ? nextMatcher.getServletPath(requestUriPath) : "";
        }

        @Override
        public String toString() {
            return "Matcher{" + "constraint='" + constraint + '\'' + ", nextMatcher=" + nextMatcher + '}';
        }
    }

    private class PrefixMatcher extends Matcher {

        private PrefixMatcher(String prefix, Matcher nextMatcher) {
            super(prefix, nextMatcher);
        }

        @Override
        protected boolean isMatching(String requestUriPath, boolean noLog) {
            boolean matches = requestUriPath.startsWith(constraint);
            if (!noLog && logger.isDebugEnabled()) {
                logger.debug("Prefix match result for servlet style uri constraint for uri path {} and constraint {} : {}",
                             requestUriPath, constraint, matches);
            }
            return matches;
        }

        @Override
        public String getServletPath(String requestUriPath) {
            if (isMatching(requestUriPath, true)) {
                return constraintWithoutContextPath.substring(0, constraintWithoutContextPath.length() - 1); // Leaving out the postfix of *, this is what we need.
            }
            return super.getServletPath(requestUriPath);
        }
    }

    private class ExtensionMatcher extends Matcher {

        private ExtensionMatcher(String constraint) {
            super(constraint.substring(1), null); // This matcher does a contains query removing the * prefix
        }

        @Override
        protected boolean isMatching(String requestUriPath, boolean noLog) {
            boolean matches = requestUriPath.contains(constraint);// The constructor removes the preciding * in the constraint.
            if (!noLog && logger.isDebugEnabled()) {
                logger.debug("Extension match result for servlet style uri constraint for uri path {} and constraint {} : {}",
                             requestUriPath, constraint, matches);
            }
            return matches;
        }

        @Override
        public String getServletPath(String requestUriPath) {
            if (isMatching(requestUriPath, true)) {
                return ""; // Extension mapping does not have servlet path.
            }
            return super.getServletPath(requestUriPath);
        }
    }

    @Override
    public String toString() {
        return "ServletStyleUriConstraintKey{" + "matcher=" + matcher + '}';
    }
}
