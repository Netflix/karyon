package com.netflix.karyon.server.http;

import com.netflix.karyon.transport.http.MethodConstraintKey;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class MethodConstraintTest extends InterceptorConstraintTestBase {

    @Test
    public void testMethodConstraint() throws Exception {
        MethodConstraintKey<ByteBuf> key = new MethodConstraintKey<ByteBuf>(HttpMethod.HEAD);
        boolean keyApplicable = doApply(key, "a/b/c", HttpMethod.HEAD);
        Assert.assertTrue("Http Method style constraint failed.", keyApplicable);
    }

    @Test
    public void testMethodConstraintFail() throws Exception {
        MethodConstraintKey<ByteBuf> key = new MethodConstraintKey<ByteBuf>(HttpMethod.HEAD);
        boolean keyApplicable = doApply(key, "a/b/c", HttpMethod.GET);
        Assert.assertFalse("Http Method style constraint failed.", keyApplicable);
    }
}
