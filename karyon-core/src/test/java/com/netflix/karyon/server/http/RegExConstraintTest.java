package com.netflix.karyon.server.http;

import com.netflix.karyon.transport.http.RegexUriConstraintKey;
import io.netty.buffer.ByteBuf;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class RegExConstraintTest extends InterceptorConstraintTestBase {

    @Test
    public void testRegExConstraint() throws Exception {
        RegexUriConstraintKey<ByteBuf> key = new RegexUriConstraintKey<ByteBuf>("a/.*");
        boolean keyApplicable = doApplyForGET(key, "a/b/c");
        Assert.assertTrue("Regex style constraint failed.", keyApplicable);
    }
}
