package com.netflix.karyon.transport.http;

/**
 * An interface for blocking HTTP based {@link com.netflix.karyon.transport.RequestRouter}s.
 *
 * @author Nitesh Kant
 */
public interface BlockingHttpRequestRouter<I, O> extends HttpRequestRouter<I, O> {
}
