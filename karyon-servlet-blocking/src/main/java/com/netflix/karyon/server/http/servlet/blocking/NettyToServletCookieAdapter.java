package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import javax.servlet.http.Cookie;

/**
 * An adapter between {@link io.netty.handler.codec.http.Cookie} and {@link Cookie}. <br/>
 * Since {@link Cookie} does not follow an interface, we have to hack around it to optimize unnecessary work in the
 * {@link Cookie} and do not delegate any method calls to it.
 *
 * @author Nitesh Kant
 */
class NettyToServletCookieAdapter extends Cookie {

    private final io.netty.handler.codec.http.Cookie decodedNettyCookie;

    public NettyToServletCookieAdapter(io.netty.handler.codec.http.Cookie decodedNettyCookie) {
        super("", ""); // Fooling the cookie implementation to not do any parsing of any kind.
        this.decodedNettyCookie = decodedNettyCookie;
        Preconditions.checkNotNull(decodedNettyCookie, "Cookie instance can not be null.");
    }

    @Override
    public void setComment(String purpose) {
        decodedNettyCookie.setComment(purpose);
    }

    @Override
    public String getComment() {
        return decodedNettyCookie.getComment();
    }

    @Override
    public void setDomain(String pattern) {
        decodedNettyCookie.setDomain(pattern);
    }

    @Override
    public String getDomain() {
        return decodedNettyCookie.getDomain();
    }

    @Override
    public void setMaxAge(int expiry) {
        decodedNettyCookie.setMaxAge(expiry);
    }

    @Override
    public int getMaxAge() {
        return (int) decodedNettyCookie.getMaxAge();
    }

    @Override
    public void setPath(String uri) {
        decodedNettyCookie.setPath(uri);
    }

    @Override
    public String getPath() {
        return decodedNettyCookie.getPath();
    }

    @Override
    public void setSecure(boolean flag) {
        decodedNettyCookie.setSecure(flag);
    }

    @Override
    public boolean getSecure() {
        return decodedNettyCookie.isSecure();
    }

    @Override
    public String getName() {
        return decodedNettyCookie.getName();
    }

    @Override
    public void setValue(String newValue) {
        decodedNettyCookie.setValue(newValue);
    }

    @Override
    public String getValue() {
        return decodedNettyCookie.getValue();
    }

    @Override
    public int getVersion() {
        return decodedNettyCookie.getVersion();
    }

    @Override
    public void setVersion(int v) {
        decodedNettyCookie.setVersion(v);
    }

    @Override
    public Object clone() {
        return Throwables.propagate(new CloneNotSupportedException("Cookie adapter does not support cloning."));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NettyToServletCookieAdapter{");
        sb.append("decodedNettyCookie=").append(decodedNettyCookie);
        sb.append('}');
        return sb.toString();
    }
}
