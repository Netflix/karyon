package com.netflix.karyon.server.spi;

import com.google.common.base.Preconditions;
import io.netty.util.concurrent.Future;

/**
 * A router that signifies the end of protocol processing and start of application processing logic. <br/>
 * <emp>A router is always non-blocking, a blocking router should instead implement {@link BlockingRequestRouter} </emp> <br/>
 * In case a router requires lifecycle callbacks, it should implement {@link LifecycleAware} <br/>
 *
 * @see com.netflix.karyon.server For the architecture of a karyon server.
 *
 * @author Nitesh Kant
 */
public interface RequestRouter<I, O> {

    /**
     * Processes a request asynchronously, the end of which is indicated by the returned {@link Future}. <br/>
     *
     * @param request Request to process.
     * @param responseWriter The response writer.
     *
     * @return The future for the processing.
     */
    Future<Void> process(I request, ResponseWriter<O> responseWriter);

    /**
     * A utility class to identify different facets of a request router, eg: blocking vs non-blocking, lifecycle aware
     * or not, etc.
     */
    final class RoutersNatureIdentifier {

        private RoutersNatureIdentifier() {
        }

        /**
         * Asserts whether the passed router is blocking.
         *
         * @param router Router to assess.
         *
         * @return {@code true} if the router implements {@link BlockingRequestRouter} interface.
         */
        public static boolean isBlocking(@SuppressWarnings("rawtypes") RequestRouter router) {
            Preconditions.checkNotNull(router, "Router instance can not be null.");
            return BlockingRequestRouter.class.isAssignableFrom(router.getClass());
        }

        /**
         * Asserts whether the passed router is lifecycle aware.
         *
         * @param router Router to assess.
         *
         * @return {@code true} if the router implements {@link LifecycleAware} interface.
         */
        public static boolean isLifecycleAware(@SuppressWarnings("rawtypes") RequestRouter router) {
            Preconditions.checkNotNull(router, "Router instance can not be null.");
            return LifecycleAware.class.isAssignableFrom(router.getClass());
        }
    }
}
