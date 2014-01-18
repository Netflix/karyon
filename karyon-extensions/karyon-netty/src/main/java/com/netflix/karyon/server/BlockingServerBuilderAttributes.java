package com.netflix.karyon.server;

/**
 * A generic guidance to what methods specifically a blocking server builder should provide over the generic
 * {@link KaryonNettyServerBuilder}
 *
 * @param <B> Any builder that uses this implementation.
 *
 * @author Nitesh Kant
 */
public interface BlockingServerBuilderAttributes<B> {

    int DEFAULT_WORKER_COUNT = 200;

    B withWorkerCount(int workerCount);
}
