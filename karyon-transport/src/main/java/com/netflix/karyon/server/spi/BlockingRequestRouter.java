package com.netflix.karyon.server.spi;

/**
 * An extension of {@link RequestRouter} to explicitly indicate a blocking router.
 *
 * @author Nitesh Kant
 */
public interface BlockingRequestRouter<I, O> extends RequestRouter<I, O> {
}
