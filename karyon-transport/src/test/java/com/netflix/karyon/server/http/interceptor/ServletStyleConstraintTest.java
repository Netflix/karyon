package com.netflix.karyon.server.http.interceptor;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class ServletStyleConstraintTest extends InterceptorConstraintTestBase {

    @Test
    public void testServletPathExactMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("d/a/b/c", "d");
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/d/a/b/c/");
        PipelineDefinition.Key.KeyEvaluationContext context = new PipelineDefinition.Key.KeyEvaluationContext();
        boolean keyApplicable = key.apply(request, context);
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
        String servletPath = key.getServletPath(request, context);
        Assert.assertEquals("Unexpected servlet path.", "/a/b/c/", servletPath);
    }

    @Test
    public void testServletPathPrefixMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("d/a/*", "d");
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/d/a/b/c/");
        PipelineDefinition.Key.KeyEvaluationContext context = new PipelineDefinition.Key.KeyEvaluationContext();
        boolean keyApplicable = key.apply(request, context);
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
        String servletPath = key.getServletPath(request, context);
        Assert.assertEquals("Unexpected servlet path.", "/a", servletPath);
    }

    @Test
    public void testServletPathExtensionMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("*.boo", "d");
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/d/a/b/c.boo");
        PipelineDefinition.Key.KeyEvaluationContext context = new PipelineDefinition.Key.KeyEvaluationContext();
        boolean keyApplicable = key.apply(request, context);
        Assert.assertTrue("Extension match servlet style constraint failed.", keyApplicable);
        String servletPath = key.getServletPath(request, context);
        Assert.assertEquals("Unexpected servlet path.", "", servletPath);
    }

    @Test
    public void testExactMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExactMatchWithTrailingSlashInUri() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExactMatchWithTrailingSlashInConstraint() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c/", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c*", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/def");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithSlashInConstraint() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c/*", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/def");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithExactUri() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c/*", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithNoSlashInUri() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c/*", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExtensionMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("*.boo", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/d.boo");
        Assert.assertTrue("Extension match servlet style constraint failed.", keyApplicable);
    }
}
