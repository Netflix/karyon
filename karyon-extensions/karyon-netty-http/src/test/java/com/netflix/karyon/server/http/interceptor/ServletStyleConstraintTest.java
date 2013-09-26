package com.netflix.karyon.server.http.interceptor;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class ServletStyleConstraintTest extends InterceptorConstraintTestBase {

    @Test
    public void testExactMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExactMatchWithTrailingSlashInUri() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExactMatchWithTrailingSlashInConstraint() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c/");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c*");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/def");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithSlashInConstraint() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c/*");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/def");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithExactUri() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c/*");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithNoSlashInUri() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c/*");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

}
