package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A collection of {@link HttpSessionImpl} objects.<br/>
 *
 * <h2>Session expirations:</h2>
 * This session manager does NOT support per session inactivity period as it does not make sense in most of the
 * applications and causes an overhead on expiration of the same. <br/>
 * This supports a fixed global (for this instance) inactivity period for sessions which is passed during construction.
 *
 * @author Nitesh Kant
 */
class HttpSessionManager {

    private static final Logger logger = LoggerFactory.getLogger(HttpSessionManager.class);

    private final int sessionInactiveTimeInSeconds;

    /**
     * A cache of sessions created by this class & expired after {@link #sessionInactiveTimeInSeconds} if not accessed.
     */
    private final Cache<String,HttpSessionImpl> sessionCache;

    HttpSessionManager(int sessionInactiveTimeInSeconds) {
        this.sessionInactiveTimeInSeconds = sessionInactiveTimeInSeconds;
        sessionCache = CacheBuilder.newBuilder()
                                   .expireAfterAccess(sessionInactiveTimeInSeconds, TimeUnit.SECONDS)
                                   .build();
    }

    /**
     * Fetches the {@link HttpSession} instance, if any, associated with the passed id.
     *
     * @param sessionId Session id.
     *
     * @return {@link HttpSession} if present for the passed id, {@code null} if none exists.
     */
    @Nullable
    HttpSessionImpl getForId(String sessionId) {
        HttpSessionImpl sessionForId = sessionCache.getIfPresent(sessionId);
        if (null != sessionForId && !sessionForId.isValid()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Session id {} invalidated by application, removing from the store & returning null. This should create a new session instance.",
                             sessionId);
            }
            sessionForId = null;
        }
        return sessionForId;
    }

    /**
     * Creates & returns a new {@link HttpSession} instance with a unique session Id.
     *
     * @return A newly created {@link HttpSession} instance.
     */
    HttpSession createNew() {
        String newSessionId = SessionIdGenerator.newId();
        HttpSessionImpl newSession = new HttpSessionImpl(newSessionId, sessionInactiveTimeInSeconds);
        sessionCache.put(newSessionId, newSession);
        if (logger.isDebugEnabled()) {
            logger.debug("Created a new HTTP session with id {}", newSessionId);
        }
        return newSession;
    }

    void clear() {
        sessionCache.invalidateAll();
    }

    private static class SessionIdGenerator {

        private static String newId() {
            return UUID.randomUUID().toString();
        }
    }
}
