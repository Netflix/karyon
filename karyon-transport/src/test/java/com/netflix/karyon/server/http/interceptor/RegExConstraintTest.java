package com.netflix.karyon.server.http.interceptor;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class RegExConstraintTest extends InterceptorConstraintTestBase {

    @Test
    public void testRegExConstraint() throws Exception {
        RegexUriConstraintKey key = new RegexUriConstraintKey("a/.*");
        boolean keyApplicable = doApplyForGET(key, "a/b/c");
        Assert.assertTrue("Regex style constraint failed.", keyApplicable);
    }
}
