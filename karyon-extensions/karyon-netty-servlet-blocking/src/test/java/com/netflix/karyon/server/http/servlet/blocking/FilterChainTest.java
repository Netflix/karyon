package com.netflix.karyon.server.http.servlet.blocking;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Nitesh Kant
 */
public class FilterChainTest {

    private static final AtomicInteger invocationOrderCounter = new AtomicInteger();

    @Before
    public void setUp() throws Exception {
        invocationOrderCounter.set(-1);
    }

    @Test
    public void testChain() throws Exception {
        List<TestableFilter> filters = new ArrayList<TestableFilter>();
        filters.add(new TestableFilter(0, invocationOrderCounter, false));
        filters.add(new TestableFilter(1, invocationOrderCounter, false));
        filters.add(new TestableFilter(2, invocationOrderCounter, false));

        ServletImpl servlet = new ServletImpl();
        FilterChainImpl chain = new FilterChainImpl(servlet, filters);
        chain.doFilter(null, null);

        for (TestableFilter filter : filters) {
            Assert.assertEquals("Unexpected invocation order for filter.", filter.indexInChain, filter.invokedOrder);
        }

        Assert.assertEquals("Unexpected invocation order for servlet.", filters.size(), servlet.invokedOrder);
    }

    private static class ServletImpl extends HttpServlet {

        private static final long serialVersionUID = 8222128486872588549L;
        private int invokedOrder;

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            invokedOrder = invocationOrderCounter.incrementAndGet();
        }
    }

}
