package netflix.karyon.transport.http;

import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides constraint implementation for {@link netflix.karyon.transport.interceptor.InterceptorKey} for matching URI paths as regular expressions as
 * supported by {@link java.util.regex.Pattern}.
 * The request URI path is as retrieved using: {@link HttpKeyEvaluationContext#getRequestUriPath(HttpServerRequest)}
 *
 * @author Nitesh Kant
 */
public class RegexUriConstraintKey<I> implements HttpInterceptorKey<I> {

    private static final Logger logger = LoggerFactory.getLogger(RegexUriConstraintKey.class);

    private final Pattern regEx;

    public RegexUriConstraintKey(String constraint) {
        if (null == constraint) {
            throw new NullPointerException("Constraint can not be null.");
        }
        regEx = Pattern.compile(constraint);
    }

    @Override
    public String toString() {
        return "RegexUriConstraintKey{" + "regEx=" + regEx + '}';
    }

    @Override
    public boolean apply(HttpServerRequest<I> request, HttpKeyEvaluationContext context) {
        String requestUriPath = context.getRequestUriPath(request);
        boolean matches = false;
        if (null != requestUriPath) {
            Matcher matcher = regEx.matcher(requestUriPath);
            matches = matcher.matches();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Result for regex based uri constraint for uri path {} and pattern {} : {}", requestUriPath,
                         regEx, matches);
        }
        return matches;
    }
}
