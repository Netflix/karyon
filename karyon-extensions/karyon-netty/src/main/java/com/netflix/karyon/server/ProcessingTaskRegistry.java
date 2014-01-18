package com.netflix.karyon.server;

import io.netty.util.concurrent.Future;

/**
 * This registry holds all the currently processing tasks by this server. The primary use of such a registry would be to
 * manage cancellation.
 *
 * @author Nitesh Kant
 */
class ProcessingTaskRegistry {

    //TODO: Manage these futures.
    // Since we do not as yet have a story around cancellation, memory overhead of this registry is not required.

    /**
     * Adds the {@link Future} instance to be managed. This will add a listener to the passed {@link Future} to remove
     * itself from the registry on completion. <br/>
     *
     * @param processingFuture The future to be added.
     */
    public void postEnqueueToRouter(@SuppressWarnings("unused") Future<Void> processingFuture) {
        // TODO: Add a listener to the future to remove itself when done
    }
}
