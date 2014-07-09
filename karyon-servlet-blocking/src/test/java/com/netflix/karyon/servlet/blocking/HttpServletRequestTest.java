package com.netflix.karyon.servlet.blocking;

import com.google.common.base.Joiner;
import com.netflix.karyon.transport.http.QueryStringDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import rx.subjects.PublishSubject;

import javax.annotation.Nullable;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * @author Nitesh Kant
 */
public class HttpServletRequestTest {

    public static final String CONTEXT_PATH = "/REST/v1/";
    public static final String SERVLET_PATH = "hello";
    public static final String REMAINING_PATH = "/abc&def";
    public static final String QUERY_PARAM_1_NAME = "id";
    public static final String QUERY_PARAM_1_VALUE = "1";
    public static final String QUERY_PARAM_2_NAME = "name";
    public static final String QUERY_PARAM_2_VALUE = "xyz";
    public static final String QUERY_STR = Joiner.on('&')
                                                 .join(Joiner.on('=').join(QUERY_PARAM_1_NAME, QUERY_PARAM_1_VALUE),
                                                       Joiner.on('=').join(QUERY_PARAM_2_NAME, QUERY_PARAM_2_VALUE));
    public static final HttpMethod HTTP_METHOD = HttpMethod.GET;
    public static final HttpVersion HTTP_VERSION = HttpVersion.HTTP_1_1;
    public static final String JSESSIONID_COOKIE_NAME = "JSESSIONID";
    @SuppressWarnings("PMD") public static final String REMOTE_ADDRESS = "1.1.1.1";
    @SuppressWarnings("PMD") public static final String LOCAL_ADDRESS = "1.0.0.1";
    public static final int SERVER_PORT = 9999;
    public static final int LOCAL_PORT = 9998;
    public static final int REMOTE_PORT = 8888;
    private static String testUri;

    static {
        final String uriNoEncode = CONTEXT_PATH + SERVLET_PATH + REMAINING_PATH + '?' + QUERY_STR;
        try {
            testUri = URLEncoder.encode(uriNoEncode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            testUri = uriNoEncode;
        }
    }

    private static final String CONTENT = "HeyBuddy!";
    private static DefaultHttpRequest nettyRequest;

    @After
    public void tearDown() throws Exception {
        nettyRequest = null;
    }

    @Test
    public void testRequestedSessionId() throws Exception {
        HttpServletRequestImpl request = createServletRequest();
        String sessionId = "sessionId";
        nettyRequest.headers().add(HttpServletRequestImpl.COOKIE_HEADER_NAME, createJsessionIdCookieValue(sessionId));
        String requestedSessionId = request.getRequestedSessionId();
        Assert.assertEquals("Unexpected requested session id", sessionId, requestedSessionId);
    }

    @Test
    public void testNoRequestedSessionId() throws Exception {
        HttpServletRequestImpl request = createServletRequest();
        String requestedSessionId = request.getRequestedSessionId();
        Assert.assertNull("Unexpected requested session id", requestedSessionId);
    }

    @Test
    public void testCookies() throws Exception {
        HttpServletRequestImpl request = createServletRequest();
        nettyRequest.headers().add(HttpServletRequestImpl.COOKIE_HEADER_NAME, "a=b;c=d");
        Cookie[] cookies = request.getCookies();
        Assert.assertNotNull("No cookies returned.", cookies);
        Assert.assertEquals("Unexpected number of cookies returned.", 2, cookies.length);
        Assert.assertEquals("Unexpected cookie name for the first cookie.", "a", cookies[0].getName());
        Assert.assertEquals("Unexpected cookie value for the first cookie.", "b", cookies[0].getValue());
        Assert.assertEquals("Unexpected cookie name for the second cookie.", "c", cookies[1].getName());
        Assert.assertEquals("Unexpected cookie value for the second cookie.", "d", cookies[1].getValue());
    }

    @Test
    public void testCookiesSingleParse() throws Exception {
        HttpServletRequestImpl request = createServletRequest();
        nettyRequest.headers().add(HttpServletRequestImpl.COOKIE_HEADER_NAME, "a=b;c=d");
        Cookie[] cookies = request.getCookies();
        Assert.assertNotNull("No cookies returned.", cookies);

        nettyRequest.headers().remove(HttpServletRequestImpl.COOKIE_HEADER_NAME); // Remove the cookie header so that it can be retrieved for second getCookie call.
        Cookie[] cookiesAfterHeaderRemove = request.getCookies();

        Assert.assertSame("Cookies seems to be parsed/created again.", cookies, cookiesAfterHeaderRemove);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidDateHeader() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String headerName = "MyDateHeader";
        nettyRequest.headers().add(headerName, "abc");
        servletRequest.getDateHeader(headerName);
    }

    @Test
    public void testValidDateHeader() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String headerName = "MyDateHeader";

        Date now = new Date();
        long timeInMillis = now.getTime();

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String nowAsStr = dateFormatGmt.format(now);

        nettyRequest.headers().add(headerName, nowAsStr);
        long dateHeader = servletRequest.getDateHeader(headerName);
        Assert.assertEquals("Date header set does not match the returned value in seconds.", timeInMillis / 1000,
                            dateHeader / 1000); /*Date header has second precision*/
    }

    @Test
    public void testGetHeader() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String headerName = "MyStrHeader";
        String headerVal = "hey!!!";
        nettyRequest.headers().add(headerName, headerVal);

        String header = servletRequest.getHeader(headerName);
        Assert.assertEquals("Header value not as expected.", headerVal, header);
    }

    @Test
    public void testGetHeaderMultiValue() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String headerName = "MyStrHeader";
        String[] headerVals = {"hey!!!", "hey123"};
        nettyRequest.headers().add(headerName, headerVals[0]);
        nettyRequest.headers().add(headerName, headerVals[1]);

        Enumeration<String> values = servletRequest.getHeaders(headerName);
        int index = 0;
        while (values.hasMoreElements()) {
            String val = values.nextElement();
            Assert.assertEquals("Header value not as expected.", headerVals[index++], val);
        }
    }

    @Test
    public void testGetHeadersWithNoHeaders() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        Enumeration<String> names = servletRequest.getHeaders("ABC");
        Assert.assertFalse("Empty enumeration expected when no headers added.", names.hasMoreElements());
    }

    @Test
    public void testGetHeaderNames() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String[] headerNames = {"MyStrHeader", "MyStrHeader2"};
        String[] headerVals = {"hey!!!", "hey123"};
        nettyRequest.headers().add(headerNames[0], headerVals[0]);
        nettyRequest.headers().add(headerNames[1], headerVals[1]);

        Enumeration<String> names = servletRequest.getHeaderNames();
        int index = 0;
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (HttpHeaders.Names.CONTENT_LENGTH.equals(name)) {
                continue;
            }
            Assert.assertEquals("Header names not as expected.", headerNames[index++], name);
        }
    }

    @Test
    public void testGetIntHeaderForMissingHeader() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String headerName = "MyIntHeader";
        int intHeader = servletRequest.getIntHeader(headerName);
        Assert.assertEquals("Non-existent header as int not as expected.", -1, intHeader);
    }
    @Test
    public void testGetIntHeader() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String headerName = "MyIntHeader";
        String headerVal = "1";
        nettyRequest.headers().add(headerName, headerVal);

        int header = servletRequest.getIntHeader(headerName);
        Assert.assertEquals("Header value not as expected.", Integer.parseInt(headerVal), header);

    }

    @Test
    public void testGetMethod() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        Assert.assertEquals("Http method not as expected.", HTTP_METHOD.name(), servletRequest.getMethod());
    }

    @Test
    public void testPathInfo() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String pathInfo = servletRequest.getPathInfo();
        Assert.assertEquals("Unexpected request path info.", REMAINING_PATH, pathInfo);
    }

    @Test
    public void testNoPathInfo() throws Exception {
        String uriNoPath = CONTEXT_PATH + SERVLET_PATH;
        HttpServletRequestImpl servletRequest = createServletRequest(uriNoPath, null);
        String pathInfo = servletRequest.getPathInfo();
        Assert.assertNull("Unexpected request path info.", pathInfo);
    }

    @Test
    public void testPathTranslated() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String pathTranslated = servletRequest.getPathTranslated();
        Assert.assertEquals("Unexpected request path translated.", REMAINING_PATH, pathTranslated);
    }

    @Test
    public void testContextPath() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String ctxPath = servletRequest.getContextPath();
        Assert.assertEquals("Unexpected request context path.", CONTEXT_PATH.substring(0, CONTEXT_PATH.length() - 1), ctxPath);
    }

    @Test
    public void testServletPath() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String servletPath = servletRequest.getServletPath();
        Assert.assertEquals("Unexpected request servlet path.", SERVLET_PATH, servletPath);
    }

    @Test
    public void testQueryString() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String queryStr = servletRequest.getQueryString();
        Assert.assertEquals("Unexpected query string.", QUERY_STR, queryStr);
    }

    @Test
    public void testNewSession() throws Exception {
        createNewSessionFromRequest(null);
    }

    @Test
    public void testExistingSession() throws Exception {
        HttpSessionManager sessionManager = new HttpSessionManager(600);
        HttpSession newSession = createNewSessionFromRequest(sessionManager);
        HttpServletRequestImpl newRequestWithOldSession = createServletRequest(testUri, sessionManager);
        nettyRequest.headers().add(HttpServletRequestImpl.COOKIE_HEADER_NAME,
                                   createJsessionIdCookieValue( newSession.getId()));

        HttpSession oldSession = newRequestWithOldSession.getSession();
        Assert.assertSame("Requested session should be same as original.", newSession, oldSession);
        Assert.assertTrue("Requested session id must be valid.", newRequestWithOldSession.isRequestedSessionIdValid());
        Assert.assertTrue("Requested session id must be from cookie.", newRequestWithOldSession.isRequestedSessionIdFromCookie());
        Assert.assertFalse("Requested session id must not be from URL.",
                           newRequestWithOldSession.isRequestedSessionIdFromURL());
    }

    @Test
    public void testAttribute() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String attr1val = "attr1val";
        String attr1Name = "attr1";
        servletRequest.setAttribute(attr1Name, attr1val);
        Assert.assertEquals("Attribute value not as expected.", attr1val, servletRequest.getAttribute(attr1Name));
        Enumeration<String> names = servletRequest.getAttributeNames();
        Assert.assertTrue("No attribute name returned.", names.hasMoreElements());
        Assert.assertEquals("Attribute name not as expected.", attr1Name, names.nextElement());
    }

    @Test
    public void testCharEncoding() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        nettyRequest.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");

        String characterEncoding = servletRequest.getCharacterEncoding();
        Assert.assertNull("Character encoding not specified but returned.", characterEncoding);

        servletRequest = createServletRequest();
        String encoding = "utf-8";
        nettyRequest.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=" + encoding);
        Assert.assertEquals("Unexpected encoding.", encoding, servletRequest.getCharacterEncoding());
    }

    @Test
    public void testCharEncodingOverride() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        nettyRequest.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");

        String characterEncoding = servletRequest.getCharacterEncoding();
        Assert.assertNull("Character encoding not specified but returned.", characterEncoding);

        String encoding = "utf-8";
        servletRequest.setCharacterEncoding(encoding);
        Assert.assertEquals("Unexpected encoding.", encoding, servletRequest.getCharacterEncoding());
    }

    @Test(expected = UnsupportedEncodingException.class)
    public void testIllegalCharEncodingOverride() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String encoding = "illegal";
        servletRequest.setCharacterEncoding(encoding);
    }

    @Test
    public void testContentLength() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        Assert.assertEquals("Unexpected request content length.", CONTENT.getBytes().length, servletRequest.getContentLength());
    }

    @Test
    public void testContentType() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String contentType = "text/html; charset=utf-8";
        nettyRequest.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
        Assert.assertEquals("Unexpected content type.", contentType, servletRequest.getContentType());
    }

    @Test
    public void testInputStream() throws Exception {
        ServletInputStream inputStream = createServletRequest().getInputStream();

        StringBuilder content;
        try {
            content = new StringBuilder();
            while (true) {
                byte[] chunk = new byte[1024];
                int bytesRead = inputStream.read(chunk);
                if (bytesRead > 0) {
                    content.append(new String(chunk, 0, bytesRead));
                } else {
                    break;
                }
            }
        } finally {
            inputStream.close();
        }

        Assert.assertEquals("Content not as expected.", CONTENT, content.toString());
    }

    @Test
    public void testReader() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        BufferedReader reader = servletRequest.getReader();
        StringBuilder content;
        try {
            content = new StringBuilder();
            while (true) {
                char[] chunk = new char[1024];
                int bytesRead = reader.read(chunk);
                if (bytesRead > 0) {
                    content.append(new String(chunk, 0, bytesRead));
                } else {
                    break;
                }
            }
        } finally {
            reader.close();
        }

        Assert.assertEquals("Content not as expected.", CONTENT, content.toString());
    }

    @Test
    public void testNoParams() throws Exception {
        String uriNoPath = CONTEXT_PATH + SERVLET_PATH;
        HttpServletRequestImpl servletRequest = createServletRequest(uriNoPath, null);
        Enumeration<String> paramNames = servletRequest.getParameterNames();
        Assert.assertNotNull("Empty enumeration expected when no query parameters are available.", paramNames);
        Assert.assertFalse("Empty enumeration expected when no query parameters are available.",
                           paramNames.hasMoreElements());

    }

    @Test
    public void testParamNames() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        Enumeration<String> paramNames = servletRequest.getParameterNames();
        Assert.assertTrue("Parameter names enum null or empty.", null != paramNames && paramNames.hasMoreElements());
        boolean containsParam1 = false;
        boolean containsParam2 = false;
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (paramName.equals(QUERY_PARAM_1_NAME)) {
                containsParam1 = true;
            } else if (paramName.equals(QUERY_PARAM_2_NAME)) {
                containsParam2 = true;
            }
        }

        Assert.assertTrue("Query param: " + QUERY_PARAM_1_NAME + " not returned as one of the query param name.",
                          containsParam1);
        Assert.assertTrue("Query param: " + QUERY_PARAM_2_NAME + " not returned as one of the query param name.",
                          containsParam2);
    }

    @Test
    public void testGetParameter() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String parameter = servletRequest.getParameter(QUERY_PARAM_1_NAME);
        Assert.assertEquals("Query parameter 1 not found.", QUERY_PARAM_1_VALUE, parameter);

        Assert.assertNull("Not found parameter should return null.", servletRequest.getParameter("BLAHAHAHAHAH"));
    }

    @Test
    public void testGetParameterValues() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String[] paramVals = servletRequest.getParameterValues(QUERY_PARAM_1_NAME);
        Assert.assertTrue("Param value for an existing param must not be null/empty",
                          null != paramVals && paramVals.length != 0);
        Assert.assertEquals("Single param value must return an array of length 1", 1, paramVals.length);
        Assert.assertEquals("Invalid param value.", QUERY_PARAM_1_VALUE, paramVals[0]);
    }

    @Test
    public void testMultiParameterValues() throws Exception {
        String secondVal = "SecondVal";
        String testUri = Joiner.on('&').join(HttpServletRequestTest.testUri,
                                             Joiner.on('=').join(QUERY_PARAM_2_NAME, secondVal));
        HttpServletRequestImpl servletRequest = createServletRequest(testUri, null);
        String[] paramVals = servletRequest.getParameterValues(QUERY_PARAM_1_NAME);
        Assert.assertNotNull("Param value for an existing param must not be null", paramVals);
        Assert.assertEquals("Single param value must return an array of length 1", 1, paramVals.length);
        Assert.assertEquals("Invalid param value.", QUERY_PARAM_1_VALUE, paramVals[0]);

        paramVals = servletRequest.getParameterValues(QUERY_PARAM_2_NAME);
        Assert.assertNotNull("Param value for an existing param must not be null", paramVals);
        Assert.assertEquals("Multi param value must return an array of length 2", 2, paramVals.length);
        Assert.assertEquals("Invalid param value.", QUERY_PARAM_2_VALUE, paramVals[0]);
        Assert.assertEquals("Invalid param value.", secondVal, paramVals[1]);
    }

    @Test
    public void testGetParamMap() throws Exception {
        String secondVal = "SecondVal";
        String testUri = Joiner.on('&').join(HttpServletRequestTest.testUri,
                                             Joiner.on('=').join(QUERY_PARAM_2_NAME, secondVal));
        HttpServletRequestImpl servletRequest = createServletRequest(testUri, null);
        Map<String,List<String>> parameterMap = servletRequest.getParameterMap();
        Assert.assertFalse("Parameter map can not be null/empty.", null == parameterMap || parameterMap.isEmpty());
        Assert.assertTrue("Parameter map must contain param: " + QUERY_PARAM_1_NAME,
                          parameterMap.containsKey(QUERY_PARAM_1_NAME));
        Assert.assertTrue("Parameter map must contain param: " + QUERY_PARAM_2_NAME,
                          parameterMap.containsKey(QUERY_PARAM_2_NAME));

        List<String> param1Values = parameterMap.get(QUERY_PARAM_1_NAME);
        Assert.assertTrue("Value for parameter 1 not as expected.", param1Values.contains(QUERY_PARAM_1_VALUE));

        List<String> param2Values = parameterMap.get(QUERY_PARAM_2_NAME);
        Assert.assertTrue("Value for parameter 2 not as expected.", param2Values.contains(QUERY_PARAM_2_VALUE));
        Assert.assertTrue("Value for parameter 2 not as expected.", param2Values.contains(secondVal));
    }

    @Test
    public void testProtocol() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        Assert.assertEquals("Unexpected request protocol.", "HTTP/1.1", servletRequest.getProtocol());
    }

    @Test
    public void testScheme() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        Assert.assertEquals("Unexpected request protocol.", "HTTP", servletRequest.getScheme());
    }

    @Test
    public void testGetServerName() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        String host = "Me";
        nettyRequest.headers().add(HttpHeaders.Names.HOST, host);
        Assert.assertEquals("Unexpected server name.", host, servletRequest.getServerName());
    }

    @Test
    public void testGetRemoteAndLocalAddreses() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        Assert.assertEquals("Unexpected server port ", SERVER_PORT, servletRequest.getServerPort());
        Assert.assertEquals("Unexpected server host", LOCAL_ADDRESS, servletRequest.getServerName());
        Assert.assertEquals("Unexpected local name", LOCAL_ADDRESS, servletRequest.getLocalName());
        Assert.assertEquals("Unexpected local address ", LOCAL_ADDRESS, servletRequest.getLocalAddr());
        Assert.assertEquals("Unexpected local port ", LOCAL_PORT, servletRequest.getLocalPort());
        Assert.assertEquals("Unexpected remote host", REMOTE_ADDRESS, servletRequest.getRemoteHost());
        Assert.assertEquals("Unexpected remote address", REMOTE_ADDRESS, servletRequest.getRemoteAddr());
        Assert.assertEquals("Unexpected remote port", REMOTE_PORT, servletRequest.getRemotePort());
    }

    @Test
    public void testLocales() throws Exception {
        HttpServletRequestImpl servletRequest = createServletRequest();
        Assert.assertEquals("No locale set, should return default locale.", Locale.getDefault(),
                            servletRequest.getLocale());
        Enumeration<Locale> locales = servletRequest.getLocales();
        Assert.assertTrue("Empty locale enum recieved.", locales.hasMoreElements());
        Assert.assertEquals("Unexpected primary locale.", Locale.getDefault(), locales.nextElement());

        servletRequest = createServletRequest();
        Locale expectedPrimaryLocale = new Locale("da");
        Locale expectedSecondaryLocale = new Locale("en", "gb");
        Locale expectedTertiaryLocale = new Locale("en");
        nettyRequest.headers().set(HttpHeaders.Names.ACCEPT_LANGUAGE,
                                   expectedPrimaryLocale.getLanguage() + ", "
                                   + expectedSecondaryLocale.getLanguage() + '-' + expectedSecondaryLocale.getCountry()
                                   + ";q=0.8, " + expectedTertiaryLocale + ";q=0.7");
        Assert.assertEquals("Preferred locale not returned.", expectedPrimaryLocale, servletRequest.getLocale());
        locales = servletRequest.getLocales();
        Assert.assertTrue("Empty locale enum recieved.", locales.hasMoreElements());
        Assert.assertEquals("Unexpected preferred locale.", expectedPrimaryLocale, locales.nextElement());
        Assert.assertEquals("Unexpected second preference locale.", expectedSecondaryLocale, locales.nextElement());
        Assert.assertEquals("Unexpected third preference locale.", expectedTertiaryLocale, locales.nextElement());

    }

    private static String createJsessionIdCookieValue(String id) {
        return Joiner.on("=").join(JSESSIONID_COOKIE_NAME, id);
    }

    private static HttpSession createNewSessionFromRequest(@Nullable HttpSessionManager sessionManager)
            throws ExecutionException, InterruptedException {
        HttpServletRequestImpl servletRequest = createServletRequest(testUri, sessionManager);
        HttpSession session = servletRequest.getSession(true);
        Assert.assertNotNull("Session creation requested, but found null.", session);
        Assert.assertTrue("Session returned must be new.", session.isNew());
        return session;
    }

    private static HttpServletRequestImpl createServletRequest() throws ExecutionException, InterruptedException {
        return createServletRequest(testUri, null);
    }

    private static HttpServletRequestImpl createServletRequest(String testUri, @Nullable HttpSessionManager sessionManager)
            throws ExecutionException, InterruptedException {
        sessionManager = null == sessionManager ? new HttpSessionManager(600) : sessionManager;
        nettyRequest = new DefaultHttpRequest(HTTP_VERSION, HTTP_METHOD, testUri);
        PublishSubject<ByteBuf> contentSub = PublishSubject.create();
        HttpServerRequest<ByteBuf> rxRequest = new HttpServerRequest<ByteBuf>(nettyRequest, contentSub);
        byte[] bytes = CONTENT.getBytes();
        nettyRequest.headers().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
        QueryStringDecoder decoder = new QueryStringDecoder(nettyRequest.getUri());
        HttpServletRequestImpl.PathComponents pathComponents =
                new HttpServletRequestImpl.PathComponents(decoder, CONTEXT_PATH, SERVLET_PATH);
        HttpServletRequestImpl httpServletRequest = new HttpServletRequestImpl(pathComponents, rxRequest,
                                                                               sessionManager,
                                                                               new MockChannelHandlerContext(
                                                                                       LOCAL_ADDRESS, SERVER_PORT,
                                                                                       LOCAL_ADDRESS, LOCAL_PORT,
                                                                                       REMOTE_ADDRESS, REMOTE_PORT),
                                                                               false);
        contentSub.onNext(Unpooled.buffer().writeBytes(bytes));
        return httpServletRequest;
    }
}
