package com.netflix.karyon.server;

import com.netflix.karyon.server.spi.BlockingRequestRouter;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * A generic guidance to what methods specifically a non-blocking server builder should provide over the generic
 * {@link KaryonNettyServerBuilder}
 *
 * @param <B> Any builder that uses this implementation.
 *
 * @author Nitesh Kant
 */
public interface NonBlockingServerBuilderAttributes<B, I, O> {

    /**
     * The number of selectors threads that will be used by netty to serve incoming requests. <br/>
     * The selector count defaults to 2 * {@link Runtime#getRuntime()#availableProcessors()} or what is configured by
     * the system property "io.netty.eventLoopThreads" <br/>
     *
     * @param selectorCount The selector count to use.
     * @return This builder.
     */
    B withSelectorCount(int selectorCount);

    /**
     * A way to specify a blocking router in a non-blocking server is to always provide an {@link EventExecutorGroup}
     *
     * @param requestRouter The router.
     * @param executorThreadCount Thread count for the executor in which this router will run.
     *
     * @return This builder.
     */
    B requestRouter(BlockingRequestRouter<I, O> requestRouter, int executorThreadCount);
}
