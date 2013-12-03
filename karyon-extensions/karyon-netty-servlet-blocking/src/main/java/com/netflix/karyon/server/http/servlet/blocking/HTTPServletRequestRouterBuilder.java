package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.netflix.karyon.server.http.interceptor.MethodConstraintKey;
import com.netflix.karyon.server.http.interceptor.PipelineDefinition;
import com.netflix.karyon.server.http.interceptor.RegexUriConstraintKey;
import com.netflix.karyon.server.http.interceptor.ServletStyleUriConstraintKey;
import io.netty.handler.codec.http.HttpMethod;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A utility builder to create a {@link HttpServletRequestRouter} with configured servlets & filters. <p/>
 *
 * @author Nitesh Kant
 */
public class HTTPServletRequestRouterBuilder {

    private String contextPath = "/";
    private String normalizedContextPathForAttachers = "";
    private boolean secure;
    private IOCFactory<Filter> filterIOCFactory = new ReflectionBasedFactory<Filter>();
    private IOCFactory<HttpServlet> servletIOCFactory = new ReflectionBasedFactory<HttpServlet>();

    private final List<ServletAndFilterAttacher> attachers = new ArrayList<ServletAndFilterAttacher>();

    public HTTPServletRequestRouterBuilder contextPath(String contextPath) {
        Preconditions.checkNotNull(contextPath, "Context path can not be null.");
        this.contextPath = contextPath;
        if (contextPath.endsWith("/")) {
            normalizedContextPathForAttachers = contextPath.substring(0, contextPath.length() - 1);
        }
        return this;
    }

    public HTTPServletRequestRouterBuilder secure() {
        secure = true;
        return this;
    }

    /**
     * Optionally specifies the {@link IOCFactory} instance to be used to instantiate any servlets configured with the
     * {@link HttpServletRequestRouter}. <br/>
     * {@link HttpServletRequestRouter} only support stateless singleton servlets & filters, so new instances will be
     * only created once, mostly during startup.
     *
     * @return This builder.
     */
    public HTTPServletRequestRouterBuilder servletFactory(IOCFactory<HttpServlet> servletIOCFactory) {
        Preconditions.checkNotNull(servletIOCFactory, "Servlet factory can not be null.");
        this.servletIOCFactory = servletIOCFactory;
        return this;
    }

    /**
     * Optionally specifies the {@link IOCFactory} instance to be used to instantiate any filters configured with the
     * {@link HttpServletRequestRouter}. <br/>
     * {@link HttpServletRequestRouter} only support stateless singleton servlets & filters, so new instances will be
     * only created once, mostly during startup.
     *
     * @return This builder.
     */
    public HTTPServletRequestRouterBuilder filterFactory(IOCFactory<Filter> filterIOCFactory) {
        Preconditions.checkNotNull(filterIOCFactory, "Filter factory can not be null.");
        this.filterIOCFactory = filterIOCFactory;
        return this;
    }

    public ServletAndFilterAttacher forUri(String uri) {
        Preconditions.checkNotNull(uri, "Uri must not be null");
        if (!uri.startsWith(normalizedContextPathForAttachers)) {
            if (uri.startsWith("/")) {
                if (uri.length() > 1) {
                    uri = uri.substring(1);
                } else {
                    uri = "";
                }
            }
            uri = Joiner.on("/").join(normalizedContextPathForAttachers, uri);
        }
        ServletAndFilterAttacher attacher =
                new ServletAndFilterAttacher(new ServletStyleUriConstraintKey(uri, normalizedContextPathForAttachers));
        attachers.add(attacher);
        return attacher;
    }

    public ServletAndFilterAttacher forUriRegex(String regEx) {
        Preconditions.checkNotNull(regEx, "URI Regular expression must not be null");
        ServletAndFilterAttacher interceptorAttacher = new ServletAndFilterAttacher(new RegexUriConstraintKey(regEx));
        attachers.add(interceptorAttacher);
        return interceptorAttacher;
    }

    public ServletAndFilterAttacher forHttpMethod(HttpMethod method) {
        Preconditions.checkNotNull(method, "Http method must not be null");
        ServletAndFilterAttacher interceptorAttacher = new ServletAndFilterAttacher(new MethodConstraintKey(method));
        attachers.add(interceptorAttacher);
        return interceptorAttacher;
    }

    public HttpServletRequestRouter build() {
        ServletsAndFiltersHolder holder = new ServletsAndFiltersHolder(filterIOCFactory, servletIOCFactory);
        for (ServletAndFilterAttacher attacher : attachers) {
            holder.configureWithAttacher(attacher);
        }
        HttpServletRequestRouter router = new HttpServletRequestRouter(contextPath, secure, holder);
        return router;
    }

    public class ServletAndFilterAttacher {

        private final PipelineDefinition.Key constraintKey;
        private Class<? extends HttpServlet> servletClass;
        private final LinkedList<Class<? extends Filter>> orderedFilters;

        ServletAndFilterAttacher(PipelineDefinition.Key constraintKey) {
            this.constraintKey = constraintKey;
            orderedFilters = new LinkedList<Class<? extends Filter>>();
        }

        /**
         * Sets the servlet class to use for serving requests matching this constraint key. <br/>
         * The servlet instance will be instantiated using the factory specified by the
         * {@link HTTPServletRequestRouterBuilder} as configured via
         * {@link HTTPServletRequestRouterBuilder#servletFactory(IOCFactory)}.
         *
         * @param servletClass Servlet class.
         *
         * @return The router builder.
         */
        public HTTPServletRequestRouterBuilder serveWith(Class<? extends HttpServlet> servletClass) {
            Preconditions.checkNotNull(servletClass, "Servlet class can not be null");
            this.servletClass = servletClass;
            return HTTPServletRequestRouterBuilder.this;
        }


        /**
         * Sets the servlet class to use for serving requests matching this constraint key. <br/>
         * The servlet instance will be instantiated using the factory specified by the
         * {@link HTTPServletRequestRouterBuilder} as configured via
         * {@link HTTPServletRequestRouterBuilder#servletFactory(IOCFactory)}.
         *
         * @param filters Filters.
         *
         * @return The router builder.
         */
        public HTTPServletRequestRouterBuilder filterWith(Class<? extends Filter>... filters) {
            Preconditions.checkNotNull(filters, "Filters array can not be null.");
            orderedFilters.addAll(Arrays.asList(filters));
            return HTTPServletRequestRouterBuilder.this;
        }


        PipelineDefinition.Key getConstraintKey() {
            return constraintKey;
        }

        Class<? extends HttpServlet> getServletClass() {
            return servletClass;
        }

        LinkedList<Class<? extends Filter>> getOrderedFilters() {
            return orderedFilters;
        }
    }

    public interface IOCFactory<T> {

        T newInstance(Class<T> toInstantiate) throws IllegalAccessException, InstantiationException;

    }

    public class ReflectionBasedFactory<T> implements IOCFactory<T> {

        @Override
        public T newInstance(Class<T> toInstantiate) throws IllegalAccessException, InstantiationException {
            Preconditions.checkNotNull(toInstantiate, "Class to instantiate can not be null.");
            return toInstantiate.newInstance();
        }
    }
}
