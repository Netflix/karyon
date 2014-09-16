package com.netflix.karyon.server.interceptor;

import com.netflix.karyon.server.MockChannelHandlerContext;
import com.netflix.karyon.transport.interceptor.InterceptorExecutor;
import com.netflix.karyon.transport.interceptor.InterceptorKey;
import com.netflix.karyon.transport.interceptor.InterceptorSupport;
import com.netflix.karyon.transport.interceptor.KeyEvaluationContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;
import rx.functions.Action0;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Nitesh Kant
 */
public class InterceptorExecutorTest {

    @Test
    public void testInbound() throws Exception {

        InterceptorKey<ByteBuf, KeyEvaluationContext> key = new MockKey(true);
        TestableInboundInterceptor interceptor1 = new TestableInboundInterceptor(key);
        TestableInboundInterceptor interceptor2 = new TestableInboundInterceptor(key);
        TestableInboundInterceptor interceptor3 = new TestableInboundInterceptor(key);

        InterceptorSupport<ByteBuf, ByteBuf, KeyEvaluationContext> support =
                new InterceptorSupport<ByteBuf, ByteBuf, KeyEvaluationContext>();
        support.forKey(key).intercept(interceptor1);
        support.forKey(key).intercept(interceptor2);
        support.forKey(key).intercept(interceptor3);

        TestableRequestRouter<ByteBuf, ByteBuf> router = new TestableRequestRouter<ByteBuf, ByteBuf>();

        InterceptorExecutor<ByteBuf, ByteBuf, KeyEvaluationContext> executor =
                new InterceptorExecutor<ByteBuf, ByteBuf, KeyEvaluationContext>(support, router);

        executeAndAwait(executor);

        Assert.assertTrue("1st interceptor did not get invoked.", interceptor1.isReceivedACall());
        Assert.assertTrue("2rd interceptor did not get invoked.", interceptor2.isReceivedACall());
        Assert.assertTrue("3rd interceptor did not get invoked.", interceptor3.isReceivedACall());
        Assert.assertTrue("Tail interceptor did not get invoked.", router.isReceivedACall());
    }

    @Test
    public void testOutbound() throws Exception {


        InterceptorKey<ByteBuf, KeyEvaluationContext> key = new MockKey(true);
        TestableOutboundInterceptor interceptor1 = new TestableOutboundInterceptor(key);
        TestableOutboundInterceptor interceptor2 = new TestableOutboundInterceptor(key);
        TestableOutboundInterceptor interceptor3 = new TestableOutboundInterceptor(key);

        InterceptorSupport<ByteBuf, ByteBuf, KeyEvaluationContext> support =
                new InterceptorSupport<ByteBuf, ByteBuf, KeyEvaluationContext>();
        support.forKey(key).intercept(interceptor1);
        support.forKey(key).intercept(interceptor2);
        support.forKey(key).intercept(interceptor3);

        TestableRequestRouter<ByteBuf, ByteBuf> router = new TestableRequestRouter<ByteBuf, ByteBuf>();

        InterceptorExecutor<ByteBuf, ByteBuf, KeyEvaluationContext> executor =
                new InterceptorExecutor<ByteBuf, ByteBuf, KeyEvaluationContext>(support, router);

        executeAndAwait(executor);

        Assert.assertTrue("1st interceptor did not get invoked.", interceptor1.isReceivedACall());
        Assert.assertTrue("2rd interceptor did not get invoked.", interceptor2.isReceivedACall());
        Assert.assertTrue("3rd interceptor did not get invoked.", interceptor3.isReceivedACall());
        Assert.assertTrue("Tail interceptor did not get invoked.", router.isReceivedACall());
    }

    protected void executeAndAwait(InterceptorExecutor<ByteBuf, ByteBuf, KeyEvaluationContext> executor)
            throws InterruptedException {
        final CountDownLatch completionLatch = new CountDownLatch(1);

        executor.execute(Unpooled.buffer(), Unpooled.buffer(),
                         new KeyEvaluationContext(new MockChannelHandlerContext("mock").channel()))
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        completionLatch.countDown();
                    }
                })
                .subscribe();

        completionLatch.await(1, TimeUnit.MINUTES);
    }

}
