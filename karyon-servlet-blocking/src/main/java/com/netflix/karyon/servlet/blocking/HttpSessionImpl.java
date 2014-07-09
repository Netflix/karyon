package com.netflix.karyon.servlet.blocking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.GuardedBy;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * An in-memory implementation of {@link HttpSession}. <br/>
 * Created and managed by {@link HttpSessionManager}
 *
 * @author Nitesh Kant
 */
class HttpSessionImpl implements HttpSession {

    private static final Logger logger = LoggerFactory.getLogger(HttpSessionImpl.class);

    private final long creationTime;
    private final String id;
    private final int maxInactiveIntervalInSeconds;
    @GuardedBy("this") private long lastAccessedTime;
    private final AttributesHolder attributesHolder;

    private volatile boolean isRequestedByClient;
    private volatile boolean invalidated;

    HttpSessionImpl(String id, int maxInactiveIntervalInSeconds) {
        this.id = id;
        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
        attributesHolder = new AttributesHolder();
        creationTime = System.currentTimeMillis();
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized long getLastAccessedTime() {
        return lastAccessedTime;
    }

    /**
     * Always returns {@code null} as we do not encourage a container abstraction, an application has the control of
     * what it wants to do.
     *
     * @return {@code null}
     */
    @Override
    public ServletContext getServletContext() {
        logger.warn("getServletContext() called on Http Session. It is not supported, returning null.");
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        // No Op.
        logger.warn("Per session inactive time is not supported. Inactive interval (in seconds) for all sessions is {}",
                    maxInactiveIntervalInSeconds);
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveIntervalInSeconds;
    }

    /**
     * Always returns {@code null} as it is anyways deprecated.
     *
     * @return {@code null}
     */
    @Override
    @Deprecated
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        logger.warn("getSessionContext() called on Http Session. It is not supported, returning null.");
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        updateLastAccessedTime();
        return attributesHolder.get(name);
    }

    @Deprecated
    @Override
    public Object getValue(String name) {
        updateLastAccessedTime();
        return attributesHolder.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        updateLastAccessedTime();
        return attributesHolder.getAttributeNames();
    }

    @Deprecated
    @Override
    public String[] getValueNames() {
        updateLastAccessedTime();
        return attributesHolder.getAttributeNamesAsArray();
    }

    @Override
    public void setAttribute(String name, Object value) {
        updateLastAccessedTime();
        attributesHolder.put(name, value);
    }

    @Deprecated
    @Override
    public void putValue(String name, Object value) {
        updateLastAccessedTime();
        attributesHolder.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        updateLastAccessedTime();
        attributesHolder.remove(name);
    }

    @Deprecated
    @Override
    public void removeValue(String name) {
        updateLastAccessedTime();
        attributesHolder.remove(name);
    }

    @Override
    public void invalidate() {
        attributesHolder.clear();
        invalidated = true;
    }

    @Override
    public boolean isNew() {
        return !isRequestedByClient;
    }

    void setRequestedByClient() {
        isRequestedByClient = true;
    }

    boolean isValid() {
        return !invalidated;
    }

    private void updateLastAccessedTime() {
        lastAccessedTime = System.currentTimeMillis();
    }
}
