package com.netflix.karyon.server.http;

import com.netflix.karyon.server.MockChannelHandlerContext;
import com.netflix.karyon.transport.http.HttpKeyEvaluationContext;
import com.netflix.karyon.transport.http.ServletStyleUriConstraintKey;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.protocol.http.UnicastContentSubject;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class ServletStyleConstraintTest extends InterceptorConstraintTestBase {

    @Test
    public void testServletPathExactMatch() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("d/a/b/c", "d");
        HttpKeyEvaluationContext context = new HttpKeyEvaluationContext(new MockChannelHandlerContext("mock"));
        HttpServerRequest<ByteBuf> request = newRequest("/d/a/b/c/");
        boolean keyApplicable = key.apply(request, context);
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
        String servletPath = key.getServletPath(request, context);
        Assert.assertEquals("Unexpected servlet path.", "/a/b/c/", servletPath);
    }

    @Test
    public void testServletPathPrefixMatch() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("d/a/*", "d");
        HttpKeyEvaluationContext context = new HttpKeyEvaluationContext(new MockChannelHandlerContext("mock"));
        HttpServerRequest<ByteBuf> request = newRequest("/d/a/b/c/");
        boolean keyApplicable = key.apply(request, context);
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
        String servletPath = key.getServletPath(request, context);
        Assert.assertEquals("Unexpected servlet path.", "/a", servletPath);
    }

    @Test
    public void testServletPathExtensionMatch() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("*.boo", "d");
        HttpKeyEvaluationContext context = new HttpKeyEvaluationContext(new MockChannelHandlerContext("mock"));
        HttpServerRequest<ByteBuf> request = newRequest("/d/a/b/c.boo");
        boolean keyApplicable = key.apply(request, context);
        Assert.assertTrue("Extension match servlet style constraint failed.", keyApplicable);
        String servletPath = key.getServletPath(request, context);
        Assert.assertEquals("Unexpected servlet path.", "", servletPath);
    }

    @Test
    public void testExactMatch() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("a/b/c", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExactMatchWithTrailingSlashInUri() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("a/b/c", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExactMatchWithTrailingSlashInConstraint() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("a/b/c/", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Exact match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatch() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("a/b/c*", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/def");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithSlashInConstraint() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("a/b/c/*", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/def");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithExactUri() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("a/b/c/*", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testPrefixMatchWithNoSlashInUri() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("a/b/c/*", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c");
        Assert.assertTrue("Prefix match servlet style constraint failed.", keyApplicable);
    }

    @Test
    public void testExtensionMatch() throws Exception {
        ServletStyleUriConstraintKey<ByteBuf> key = new ServletStyleUriConstraintKey<ByteBuf>("*.boo", "");
        boolean keyApplicable = doApplyForGET(key, "/a/b/c/d.boo");
        Assert.assertTrue("Extension match servlet style constraint failed.", keyApplicable);
    }

    protected HttpServerRequest<ByteBuf> newRequest(String uri) {
        return new HttpServerRequest<ByteBuf>(new DefaultHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, uri),
                                              UnicastContentSubject.<ByteBuf>createWithoutNoSubscriptionTimeout());
    }
}
