package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.netflix.karyon.transport.http.HttpKeyEvaluationContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.simpleframework.http.parse.ContentTypeParser;
import org.simpleframework.http.parse.LanguageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of {@link HttpServletRequest} to suit our needs. <br/>
 * Javadocs on each method provides the details of the implementation and call out cases which are not supported.
 *
 * @author Nitesh Kant
 */
public class HttpServletRequestImpl implements HttpServletRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRequestImpl.class);
    public static final String COOKIE_HEADER_NAME = "Cookie";
    public static final Enumeration<String> EMPTY_ENUMERATION = Iterators.asEnumeration(Iterators.<String>emptyIterator());
    public static final String DEFAULT_CHARACTER_ENCODING = "ISO-8859-1";

    public static final int NON_EXISTENT_HEADER_AS_INT = -1;

    private static final HttpDateHeaderHandlerImpl HTTP_DATE_HEADER_HANDLER = new HttpDateHeaderHandlerImpl();
    private static final SessionCorrelator SESSION_CORRELATOR = new CookieBasedCorrelator();
    public static List<Locale> defaultLocaleList = Collections.singletonList(Locale.getDefault());

    static {
        try {
            defaultLocaleList = Collections.singletonList(Locale.getDefault());
        } catch (Exception e) {
            logger.error("Exception retrieving the default server locale.", e);
            defaultLocaleList = Collections.emptyList();
        }
    }

    private final HttpServerRequest<ByteBuf> serverRequest;

    private final String scheme;
    private final String contextPath;
    private final String requestUri;
    private final AttributesHolder attributesHolder;

    private final String protocolWithVersionString;
    @Nullable private final String servletPath;
    @Nullable private final String queryString;
    @Nullable private final String pathInfo;

    @Nullable private final ChannelHandlerContext channelHandlerContext;

    private final HttpSessionManager sessionManager;
    private final AtomicReference<HttpSession> associatedSession = new AtomicReference<HttpSession>(null);

    @GuardedBy("this") @Nullable private Cookie[] cookies;
    @GuardedBy("this") private boolean cookiesParsed; // Don't need to be a volatile as it is always accessed in a sync lock.

    private final Enumeration<String> headerNamesEnum;
    @Nullable private String localName;
    @Nullable private String localAddress;

    private int localPort;
    private int remotePort;
    @Nullable private String remoteHost;
    private int serverPort;
    @Nullable private String remoteAddr;

    @GuardedBy("this") @Nullable private String requestedSessionId;
    @GuardedBy("this") @Nullable private boolean sessionCookiesParsed;
    @GuardedBy("this") @Nullable private LanguageParser languageParser;
    @GuardedBy("this") @Nullable private ContentTypeParser contentTypeParser;
    @Nullable private String characterEncodingOverride;
    private ByteBuf content;

    @VisibleForTesting
    HttpServletRequestImpl(PathComponents pathComponents, HttpServerRequest<ByteBuf> serverRequest,
                           HttpSessionManager sessionManager, boolean isSecure)
            throws ExecutionException, InterruptedException {
        this(pathComponents, serverRequest, sessionManager, null, isSecure);
    }

    public HttpServletRequestImpl(PathComponents pathComponents, HttpServerRequest<ByteBuf> serverRequest,
                                  HttpSessionManager sessionManager,
                                  @Nullable ChannelHandlerContext channelHandlerContext, boolean isSecure)
            throws ExecutionException, InterruptedException {
        Preconditions.checkNotNull(pathComponents, "Path components can not be null.");
        Preconditions.checkNotNull(serverRequest, "Netty request can not be null,");
        this.sessionManager = sessionManager;
        this.channelHandlerContext = channelHandlerContext;
        contextPath = pathComponents.getContextPath();
        pathInfo = pathComponents.getPathInfo();
        servletPath = pathComponents.getServletPath();
        queryString = pathComponents.getQueryString();
        requestUri = pathComponents.getRequestUri();
        attributesHolder = new AttributesHolder();
        HttpVersion protocolVersion = serverRequest.getHttpVersion();
        scheme = isSecure ? "HTTPS" : "HTTP";
        protocolWithVersionString = Joiner.on("/").join(scheme,
                                                        Joiner.on(".")
                                                              .join(protocolVersion.majorVersion(),
                                                                    protocolVersion.minorVersion()));
        populatePortAndHostInfo(channelHandlerContext != null ? channelHandlerContext.channel() : null);
        this.serverRequest = serverRequest;
        Set<String> headerNames = serverRequest.getHeaders().names();
        if (!headerNames.isEmpty()) {
            headerNamesEnum = Iterators.asEnumeration(headerNames.iterator());
        } else {
            headerNamesEnum = EMPTY_ENUMERATION;
        }
        content = serverRequest.getContent().toBlocking().toFuture().get();
    }

    /**
     * Always returns {@code null} as we do not support servlet authentication.
     *
     * @return {@code null}
     */
    @Override
    public String getAuthType() {
        logger.warn("getAuthType() called on servlet request. Security is not supported, returning null.");
        return null;
    }

    /**
     * Uses {@link CookieDecoder} to decode the header with name {@link #COOKIE_HEADER_NAME}, if available. <br/>
     * Returns instances of {@link NettyToServletCookieAdapter} for every {@link io.netty.handler.codec.http.Cookie}
     * instance returned by the decoder.
     *
     * @return Instances of {@link NettyToServletCookieAdapter} for every available cookie. {@code null} if no cookies
     * exist.
     */
    @Override
    @Nullable
    public /*synchronizes cookies instance creation*/synchronized Cookie[] getCookies() {
        if (null == cookies && !cookiesParsed) {
            cookiesParsed = true; // Even if there was a failure, don't try to parse again as nothing will change.
            String cookieHeader = serverRequest.getHeaders().get(COOKIE_HEADER_NAME);
            if (null != cookieHeader) {
                Set<io.netty.handler.codec.http.Cookie> decodedNettyCookies = CookieDecoder.decode(cookieHeader);
                if (!decodedNettyCookies.isEmpty()) {
                    cookies = new Cookie[decodedNettyCookies.size()];
                    int cookieArrayIndex = 0;
                    for (io.netty.handler.codec.http.Cookie decodedNettyCookie : decodedNettyCookies) {
                        cookies[cookieArrayIndex++] = new NettyToServletCookieAdapter(decodedNettyCookie);
                    }
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Cookies already parsed, so not parsing again. cookiesParsed flag: {} Cookies size: {}",
                             cookiesParsed, null != cookies ? cookies.length : "null");
            }
        }
        return cookies;
    }

    @Override
    public long getDateHeader(String name) {
        return HTTP_DATE_HEADER_HANDLER.getDateAsMillisFromEpoch(serverRequest.getHeaders(), name);
    }

    @Override
    public String getHeader(String name) {
        return serverRequest.getHeaders().get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> allValues = serverRequest.getHeaders().getAll(name);
        if (!allValues.isEmpty()) {
            final Iterator<String> iterator = allValues.iterator();
            return Iterators.asEnumeration(iterator);
        } else {
            return EMPTY_ENUMERATION;
        }
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return headerNamesEnum;
    }

    @Override
    public int getIntHeader(String name) {
        String intHeaderAsString = serverRequest.getHeaders().get(name);
        if (null == intHeaderAsString) {
            return NON_EXISTENT_HEADER_AS_INT;
        }

        return Integer.parseInt(intHeaderAsString);
    }

    @Override
    public String getMethod() {
        return serverRequest.getHttpMethod().name();
    }

    @Nullable
    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    /**
     * Same as {@link #getPathInfo()} as this module does not deal with resources other than servlets and there is no
     * difference between these two methods with respect to servlets.
     *
     * @return Path as returned by {@link #getPathInfo()}
     */
    @Override
    public String getPathTranslated() {
        return getPathInfo();
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Nullable
    @Override
    public String getQueryString() {
        return queryString;
    }

    /**
     * Always returns {@code null} as we do not support authentication.
     *
     * @return {@code null}
     */
    @Override
    public String getRemoteUser() {
        logger.warn("getRemoteUser() called on servlet request. Security is not supported, returning null.");
        return null;
    }

    /**
     * Always returns {@code false} as we do not support authentication.
     *
     * @return {@code false}
     */
    @Override
    public boolean isUserInRole(String role) {
        logger.warn("isUserInRole() called on servlet request. Security is not supported, returning false.");
        return false;
    }

    /**
     * Always returns {@code null} as we do not support authentication.
     *
     * @return {@code null}
     */
    @Override
    public Principal getUserPrincipal() {
        logger.warn("getUserPrincipal() called on servlet request. Security is not supported, returning null.");
        return null;
    }

    @Override
    public /*Lazy cookie parsing to get session id hence sync*/ synchronized String getRequestedSessionId() {
        if (!sessionCookiesParsed) {
            requestedSessionId = SESSION_CORRELATOR.getSessionIdForRequest(this);
            sessionCookiesParsed = true;
        }
        return requestedSessionId;
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    /**
     * Not supported, always returns {@code null}
     *
     * @return {@code null}
     */
    @Override
    public StringBuffer getRequestURL() {
        logger.warn("getRequestURL() called on servlet request. This is not supported, returning null.");
        return null;
    }

    @Nullable
    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        HttpSession sessionForId = getSession();
        if (create && null == sessionForId) {
            HttpSession newSession = sessionManager.createNew();
            if (associatedSession.compareAndSet(null, newSession)) {
                return newSession;
            } else {
                return associatedSession.get(); // We assume here that we do not set session to null once it is created.
            }
        }

        return sessionForId;
    }

    @Override
    public HttpSession getSession() {
        HttpSession associatedSession = this.associatedSession.get();
        if (associatedSession != null) {
            return associatedSession;
        }

        String _requestedSessionId = getRequestedSessionId();
        HttpSessionImpl sessionForId = null;
        if (null != _requestedSessionId) {
            sessionForId = sessionManager.getForId(_requestedSessionId);
            if (null != sessionForId) {
                sessionForId.setRequestedByClient(); // Since, this session is found for the session id requested by
                // client, by definition it is not new.
            }
        }
        return sessionForId;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        HttpSession requestedSession = getSession();
        return null != requestedSession;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        String _requestedSessionId = getRequestedSessionId();
        return null != _requestedSessionId;// We only retrieve this from cookie.
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;// We only retrieve this from cookie.
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public Object getAttribute(String name) {
        return attributesHolder.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return attributesHolder.getAttributeNames();
    }

    @Override
    public /*Lazy header parsing to get char encoding hence sync*/ synchronized String getCharacterEncoding() {
        if (null != characterEncodingOverride) {
            return characterEncodingOverride;
        }
        if (null == contentTypeParser) {
            String contentTypeHeader = serverRequest.getHeaders().get(HttpHeaders.Names.CONTENT_TYPE);
            contentTypeParser = new ContentTypeParser(contentTypeHeader); // if the header is not present, the parser will return nulls.
        }
        return contentTypeParser.getCharset();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        try {
            if (!Charset.isSupported(env)) {
                logger.error("Unsupported character encoding " + env + " supplied for the request.");
                throw new UnsupportedEncodingException("Unsupported character encoding " + env + " supplied for the request.");
            }

            characterEncodingOverride = env;
        } catch (IllegalCharsetNameException e) {
            logger.error("Illegal character encoding supplied for the request.", e);
            throw new UnsupportedEncodingException("Illegal character encoding " + env + " supplied for the request.");
        }
    }

    @Override
    public int getContentLength() {
        return (int) serverRequest.getHeaders().getContentLength();
    }

    @Override
    public String getContentType() {
        return serverRequest.getHeaders().get(HttpHeaders.Names.CONTENT_TYPE);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteBufInputStream byteBufIs = getContentAsStream();
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return byteBufIs.read();
            }
        };
    }

    @Override
    public String getParameter(String name) {
        String[] paramValues = getParameterValues(name);
        return null != paramValues ? paramValues[0] : null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        Map<String, List<String>> parameters = getParameterMap();
        return Iterators.asEnumeration(parameters.keySet().iterator());
    }

    @Override
    public String[] getParameterValues(String name) {
        Map<String, List<String>> parameters = getParameterMap();
        List<String> values = parameters.get(name);
        return null != values && !values.isEmpty() ? values.toArray(new String[values.size()]) : null;
    }

    @Override
    public Map<String, List<String>> getParameterMap() {
        com.netflix.karyon.transport.http.QueryStringDecoder queryStrDecoder =
                HttpKeyEvaluationContext.getOrCreateQueryStringDecoder(serverRequest, channelHandlerContext);
        if (null == queryStrDecoder) {
            queryStrDecoder = new com.netflix.karyon.transport.http.QueryStringDecoder(serverRequest.getUri());
            if (channelHandlerContext != null) {
                channelHandlerContext.attr(HttpKeyEvaluationContext.queryDecoderKey).setIfAbsent(queryStrDecoder);
            }
        }
        return queryStrDecoder.nettyDecoder().parameters();
    }

    @Override
    public String getProtocol() {
        return protocolWithVersionString;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public String getServerName() {
        String hostHeader = getHeader(HttpHeaders.Names.HOST);
        return null != hostHeader ? hostHeader : localName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getContentAsStream(), getCharacterEncodingWithDefault()));
    }

    @Nullable
    @Override
    public String getRemoteAddr() {
        return remoteAddr;
    }

    @Nullable
    @Override
    public String getRemoteHost() {
        return remoteHost;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributesHolder.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        attributesHolder.remove(name);
    }

    @Override
    public Locale getLocale() {
        Enumeration<Locale> locales = getLocales();
        return locales.hasMoreElements() ? locales.nextElement() : Locale.getDefault(); // If there was an error getting locales then the enum will be empty.
    }

    @Override
    public /*Lazy header parsing to get locales hence sync*/ synchronized Enumeration<Locale> getLocales() {
        if (null == languageParser) {
            String acceptLanguageHeader = serverRequest.getHeaders().get(HttpHeaders.Names.ACCEPT_LANGUAGE);
            languageParser = new LanguageParser(acceptLanguageHeader); // if header is not present, then this will be an empty parser, returning nulls
        }

        List<Locale> locales = languageParser.list();
        if (null != locales && !locales.isEmpty()) {
            return Iterators.asEnumeration(locales.iterator());
        }
        return Iterators.asEnumeration(defaultLocaleList.iterator());
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        logger.warn("getRequestDispatcher() called on servlet request, it is not supported, returning null. Path requested: " + path);
        return null;
    }

    /**
     * Not supported, always returns {@code null}
     *
     * @return {@code null}
     */
    @Override
    @Deprecated
    public String getRealPath(String path) {
        logger.warn("getRealPath() called on servlet request, it is not supported, returning null.");
        return null;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Nullable
    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public String getLocalAddr() {
        return localAddress;
    }

    @Override
    public int getLocalPort() {
        return localPort;
    }

    private void populatePortAndHostInfo(@Nullable Channel channel) {
        if (null == channel) {
            return;
        }
        SocketAddress serverSocketAddr = channel.parent().localAddress();
        if (null != serverSocketAddr && InetSocketAddress.class.isAssignableFrom(serverSocketAddr.getClass())) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) serverSocketAddr;
            serverPort = inetSocketAddress.getPort();
        }
        SocketAddress localSocketAddress = channel.localAddress();
        if (null != localSocketAddress && InetSocketAddress.class.isAssignableFrom(localSocketAddress.getClass())) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) localSocketAddress;
            localName = inetSocketAddress.getHostName();
            localPort = inetSocketAddress.getPort();
            localAddress = null == inetSocketAddress.getAddress() ? null : inetSocketAddress.getAddress().getHostAddress();
        }

        SocketAddress remoteSocketAddr = channel.remoteAddress();
        if (null != remoteSocketAddr && InetSocketAddress.class.isAssignableFrom(remoteSocketAddr.getClass())) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteSocketAddr;
            remotePort = inetSocketAddress.getPort();
            remoteHost = inetSocketAddress.getHostName();
            remoteAddr = null == inetSocketAddress.getAddress() ? null : inetSocketAddress.getAddress().getHostAddress();
        }
    }

    public String getCharacterEncodingWithDefault() {
        String characterEncoding = getCharacterEncoding();
        return null != characterEncoding ? characterEncoding : DEFAULT_CHARACTER_ENCODING;
    }

    @VisibleForTesting
    HttpServerRequest<ByteBuf> rxRequest() {
        return serverRequest;
    }

    protected ByteBufInputStream getContentAsStream() {
        return new ByteBufInputStream(content);
    }

    public static class PathComponents {

        private final String contextPath;
        private final String requestUri;
        @Nullable private final String servletPath;
        @Nullable private final String queryString;
        @Nullable private final String pathInfo;

        public PathComponents(com.netflix.karyon.transport.http.QueryStringDecoder queryStringDecoder,
                              @Nullable String _contextPath, @Nullable String _servletPath) {
            Preconditions.checkNotNull(queryStringDecoder, "Query string decoder can not be null");

            final String uri = QueryStringDecoder.decodeComponent(queryStringDecoder.uri());

            contextPath = null == _contextPath ? "" : _contextPath.endsWith("/")
                                                      ? _contextPath.substring(0, _contextPath.length() - 1)
                                                      : _contextPath;
            servletPath = null == _servletPath ? "" : _servletPath;
            requestUri = queryStringDecoder.nettyDecoder().path();

            final String normalizedServletPathPath = servletPath.startsWith("/")
                                                     ? servletPath.length() > 1
                                                       ? servletPath.substring(1)
                                                       : ""
                                                     : servletPath;

            String uriPath = queryStringDecoder.nettyDecoder().path();
            String contextAndServletPath = Joiner.on("/").join(contextPath, normalizedServletPathPath);
            if (uriPath.startsWith(contextAndServletPath)) {
                String _pathInfo = uriPath.substring(contextAndServletPath.length());
                if (_pathInfo.isEmpty() || "/".equals(_pathInfo)) {
                    _pathInfo = null; // As per the getPathInfo() contract
                }
                pathInfo = _pathInfo;
            } else {
                pathInfo = uriPath; // This should not happen unless its a bug.
            }

            int pathEndIndex = uri.indexOf('?');
            if (pathEndIndex > 0 && pathEndIndex != uri.length() /*URI just contains a / */) {
                queryString = uri.substring(pathEndIndex + 1);
            } else {
                queryString = null;
            }
        }

        public String getContextPath() {
            return contextPath;
        }

        public String getRequestUri() {
            return requestUri;
        }

        @Nullable
        public String getServletPath() {
            return servletPath;
        }

        @Nullable
        public String getQueryString() {
            return queryString;
        }

        @Nullable
        public String getPathInfo() {
            return pathInfo;
        }
    }
}
