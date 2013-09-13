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
public class ServletStyleConstraintTest {

    @Test
    public void testExactMatch() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c");
        boolean keyApplicable = doApply(key, "/a/b/c");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExactMatchWithTrailingSlashInUri() throws Exception {
        ServletStyleUriConstraintKey key = new ServletStyleUriConstraintKey("a/b/c");
        boolean keyApplicable = doApply(key, "/a/b/c/");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    private static boolean doApply(ServletStyleUriConstraintKey key, String uri) {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        return key.apply(request, new PipelineDefinition.Key.KeyEvaluationContext());
    }
}
