package com.netflix.karyon.server.http.servlet.blocking;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * @author Nitesh Kant
 */
public class ServletAndFilterHolderTest {

    @Test
    public void testServletPathMapping() throws Exception {
        testAServletBinding("/a/b/*", "/a/b/c", false);
    }

    @Test
    public void testExtensionMapping() throws Exception {
        testAServletBinding("*.blah", "/a/b/c.blah", false);
    }

    @Test
    public void testExactMatch() throws Exception {
        testAServletBinding("a/b", "a/b", false);
    }

    @Test
    public void testRegExServletMatch() throws Exception {
        testAServletBinding(".*", "a/b", true);
    }

    @Test
    public void testExactFilterMatch() throws Exception {
        testAFilterBinding("a/b", "a/b", false);
    }

    @Test
    public void testRegExFilterMatch() throws Exception {
        testAFilterBinding(".*", "a/b", true);
    }

    @Test
    public void testHttpMethodMatch() throws Exception {
        HTTPServletRequestRouterBuilder builder = createRouterBuilder(new TestableServlet(), new TestableFilter());
        builder.forHttpMethod(HttpMethod.GET).serveWith(TestableServlet.class);
        builder.forHttpMethod(HttpMethod.GET).filterWith(TestableFilter.class);
        HttpServletRequestRouter router = builder.build();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "");
        ServletsAndFiltersHolder.ServletMatchResult result = router.servletsAndFiltersHolder().getMatchingServlet(
                request, null);
        Assert.assertNotNull("No matching servlet found.", result);
        Assert.assertEquals("Unexpected matching servlet found.", TestableServlet.class, result.servlet().getClass());

        List<Filter> filters = router.servletsAndFiltersHolder().getMatchingFilters(request, null);
        Assert.assertNotNull("No matching filters found.", filters);
        Assert.assertEquals("Unexpected number of matching filters found.", 1, filters.size());
        Assert.assertEquals("Unexpected matching filter found.", TestableFilter.class, filters.get(0).getClass());
    }

    private static void testAServletBinding(String constraint, String uri, boolean isRegex) {
        HTTPServletRequestRouterBuilder builder = createRouterBuilder(new TestableServlet(), null);
        if (isRegex) {
            builder.forUriRegex(constraint).serveWith(TestableServlet.class);
        } else {
            builder.forUri(constraint).serveWith(TestableServlet.class);
        }
        HttpServletRequestRouter router = builder.build();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        ServletsAndFiltersHolder.ServletMatchResult result = router.servletsAndFiltersHolder().getMatchingServlet(
                request, null);
        Assert.assertNotNull("No matching servlet found.", result);
        Assert.assertEquals("Unexpected matching servlet found.", TestableServlet.class, result.servlet().getClass());
    }

    private static void testAFilterBinding(String constraint, String uri, boolean isRegex) {
        HTTPServletRequestRouterBuilder builder = createRouterBuilder(new TestableServlet(), null);
        if (isRegex) {
            builder.forUriRegex(constraint).filterWith(TestableFilter.class);
        } else {
            builder.forUri(constraint).filterWith(TestableFilter.class);
        }
        HttpServletRequestRouter router = builder.build();
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        List<Filter> filters = router.servletsAndFiltersHolder().getMatchingFilters(request, null);
        Assert.assertNotNull("No matching filters found.", filters);
        Assert.assertEquals("Unexpected number of matching filters found.", 1, filters.size());
        Assert.assertEquals("Unexpected matching filter found.", TestableFilter.class, filters.get(0).getClass());
    }

    private static HTTPServletRequestRouterBuilder createRouterBuilder(@Nullable final HttpServlet servlet,
                                                                       @Nullable final Filter filter) {
        HTTPServletRequestRouterBuilder builder = new HTTPServletRequestRouterBuilder();
        if (null != servlet) {
            builder.servletFactory(new HTTPServletRequestRouterBuilder.IOCFactory<HttpServlet>() {
                @Override
                public HttpServlet newInstance(Class<HttpServlet> toInstantiate) {
                    return servlet;
                }
            });
        }
        if (null != filter) {
            builder.filterFactory(new HTTPServletRequestRouterBuilder.IOCFactory<Filter>() {
                @Override
                public Filter newInstance(Class<Filter> toInstantiate) {
                    return filter;
                }
            });
        }
        return builder;
    }

    public static class TestableFilter implements Filter, Serializable {

        private static final long serialVersionUID = 1779057897162088887L;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
        }

        @Override
        public void destroy() {
        }
    }

}
