package com.netflix.karyon.servlet.blocking;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.netflix.karyon.transport.http.HttpInterceptorKey;
import com.netflix.karyon.transport.http.HttpKeyEvaluationContext;
import com.netflix.karyon.transport.http.ServletStyleUriConstraintKey;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A holder of Http servlets and filters used by {@link HttpServletRequestRouter}. <br/>
 * Unless you have any specific reasons, it is better to use {@link HTTPServletRequestRouterBuilder} to create a
 * {@link HttpServletRequestRouter} rather than handling this holder directly.
 *
 * @author Nitesh Kant
 */
class ServletsAndFiltersHolder {

    private static final Logger logger = LoggerFactory.getLogger(ServletsAndFiltersHolder.class);

    private final ListMultimap<HttpInterceptorKey<ByteBuf>, Class<? extends Filter>> constraintVsFilters =
            Multimaps.newListMultimap(new LinkedHashMap<HttpInterceptorKey<ByteBuf>, Collection<Class<? extends Filter>>>(),
                                      new Supplier<List<Class<? extends Filter>>>() {
                                          @Override
                                          public List<Class<? extends Filter>> get() {
                                              return new ArrayList<Class<? extends Filter>>();
                                          }
                                      });

    private final Map<HttpInterceptorKey<ByteBuf>, Class<? extends HttpServlet>> constraintVsServlet =
            new HashMap<HttpInterceptorKey<ByteBuf>, Class<? extends HttpServlet>>();

    private final HTTPServletRequestRouterBuilder.IOCFactory<? extends Filter> filterIOCFactory;
    private final HTTPServletRequestRouterBuilder.IOCFactory<? extends HttpServlet> servletIOCFactory;

    @SuppressWarnings("rawtypes")
    private final LoadingCache<Class, Object> instanceCache =
            CacheBuilder.newBuilder().build(new CacheLoader<Class, Object>() {

                @Override
                @SuppressWarnings("unchecked")
                public Object load(Class key) throws Exception {
                    if (Filter.class.isAssignableFrom(key)) {
                        return filterIOCFactory.newInstance(key);
                    } else {
                        return servletIOCFactory.newInstance(key);
                    }
                }
            });

    ServletsAndFiltersHolder(HTTPServletRequestRouterBuilder.IOCFactory<Filter> filterIOCFactory,
                                    HTTPServletRequestRouterBuilder.IOCFactory<HttpServlet> servletIOCFactory) {
        Preconditions.checkNotNull(filterIOCFactory, "Filter IOC factory can not be null.");
        Preconditions.checkNotNull(servletIOCFactory, "Servlet IOC factory can not be null.");
        this.filterIOCFactory = filterIOCFactory;
        this.servletIOCFactory = servletIOCFactory;
    }

    void configureWithAttacher(HTTPServletRequestRouterBuilder.ServletAndFilterAttacher attacher) {
        HttpInterceptorKey<ByteBuf> constraintKey = attacher.getConstraintKey();
        LinkedList<Class<? extends Filter>> orderedFilters = attacher.getOrderedFilters();
        if (!orderedFilters.isEmpty()) {
            constraintVsFilters.putAll(constraintKey, orderedFilters);
        }
        Class<? extends HttpServlet> servletClass = attacher.getServletClass();
        if (null != servletClass) {
            Class<? extends HttpServlet> oldValue = constraintVsServlet.put(constraintKey, servletClass);
            if (null != oldValue) {
                logger.warn("Duplicate servlet mappings for constraint {}. The configured servlet now is {}",
                            oldValue, servletClass);
            }
        }
    }

    /**
     * Evaluate the passed request to see which servlet best matches the request.
     *
     * @param httpRequest Http request for which the servlet is to be found.
     * @param handlerContext Channel handler context.
     *
     * @return The servlet matching result container, {@code null} if none matches.
     */
    @Nullable
    ServletMatchResult getMatchingServlet(HttpServerRequest<ByteBuf> httpRequest, ChannelHandlerContext handlerContext) {
        HttpKeyEvaluationContext keyEvaluationContext = new HttpKeyEvaluationContext(handlerContext);
        for (Map.Entry<HttpInterceptorKey<ByteBuf>, Class<? extends HttpServlet>> servletEntry : constraintVsServlet.entrySet()) {
            HttpInterceptorKey<ByteBuf> key = servletEntry.getKey();
            if (key.apply(httpRequest, keyEvaluationContext)) {
                String servletPath = null;
                if (key instanceof ServletStyleUriConstraintKey) {
                    ServletStyleUriConstraintKey<ByteBuf> servletStyleUriConstraintKey = (ServletStyleUriConstraintKey<ByteBuf>) key;
                    servletPath = servletStyleUriConstraintKey.getServletPath(httpRequest, keyEvaluationContext);
                }
                HttpServlet servlet = fromInstanceCache(servletEntry.getValue());
                return new ServletMatchResult(servletPath, servlet);
            }
        }
        return null;
    }

    List<Filter> getMatchingFilters(HttpServerRequest<ByteBuf> httpRequest, ChannelHandlerContext handlerContext) {
        List<Filter> toReturn = new ArrayList<Filter>();
        HttpKeyEvaluationContext keyEvaluationContext = new HttpKeyEvaluationContext(handlerContext);
        for (Map.Entry<HttpInterceptorKey<ByteBuf>, Collection<Class<? extends Filter>>> filterEntry : constraintVsFilters.asMap().entrySet()) {
            if (filterEntry.getKey().apply(httpRequest, keyEvaluationContext)) {
                Collection<Class<? extends Filter>> filterClasses = filterEntry.getValue();
                for (Class<? extends Filter> filterClass : filterClasses) {
                    Filter filter = fromInstanceCache(filterClass);
                    if (null != filter) {
                        toReturn.add(filter);
                    }
                }
            }
        }
        return toReturn;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> T fromInstanceCache(Class clazz) {
        try {
            return (T) instanceCache.get(clazz);
        } catch (ExecutionException e) {
            logger.error("Error fetching instance of class " + clazz + " from the instance cache. Returning null.", e);
            return null;
        }
    }

    static class ServletMatchResult {

        private final String servletPath;
        private final HttpServlet servlet;

        ServletMatchResult(String servletPath, HttpServlet servlet) {
            this.servletPath = servletPath;
            this.servlet = servlet;
        }

        String servletPath() {
            return servletPath;
        }

        HttpServlet servlet() {
            return servlet;
        }
    }
}
