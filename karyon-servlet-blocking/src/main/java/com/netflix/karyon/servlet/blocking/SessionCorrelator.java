package com.netflix.karyon.servlet.blocking;

import io.netty.handler.codec.http.HttpRequest;

import javax.annotation.Nullable;

/**
 * This contract determines how to correlate which session Id belongs to which
 * {@link HttpRequest}.
 *
 * @author Nitesh Kant
 */
public interface SessionCorrelator {

    /**
     * Retrieves a session id, if any, for the passed request.
     *
     * @param htpRequest Http request for which the session id is to be found.
     *
     * @return Session id associated with the request, {@code null} if none exists.
     */
    @Nullable
    String getSessionIdForRequest(HttpServletRequestImpl htpRequest);
}
