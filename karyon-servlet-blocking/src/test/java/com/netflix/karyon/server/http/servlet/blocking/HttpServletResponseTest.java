package com.netflix.karyon.server.http.servlet.blocking;

import com.google.common.base.Joiner;
import com.netflix.karyon.transport.http.QueryStringDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.metrics.MetricEventsSubject;
import io.reactivex.netty.protocol.http.server.HttpResponseHeaders;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.server.ServerMetricsEvent;
import org.junit.Assert;
import org.junit.Test;
import rx.subjects.PublishSubject;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * @author Nitesh Kant
 */
public class HttpServletResponseTest {

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

    @Test
    public void testCookie() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();

        String cookie1Name = "cookie1";
        String cookie1val = "cookie1val";
        String domain = "abc.com";
        String comment = "comment";
        String path = "/";
        int age = 1;
        Cookie cookieToAdd = new Cookie(cookie1Name, cookie1val);
        cookieToAdd.setDomain(domain);
        cookieToAdd.setComment(comment);
        cookieToAdd.setPath(path);
        cookieToAdd.setMaxAge(age);
        cookieToAdd.setVersion(1);
        servletResponse.addCookie(cookieToAdd);
        Assert.assertTrue("Set-Cookie header must be present after adding a cookie",
                          servletResponse.containsHeader(HttpHeaders.Names.SET_COOKIE));
        String setCookieVal = servletResponse.serverResponse().getHeaders().get(HttpHeaders.Names.SET_COOKIE);
        Set<io.netty.handler.codec.http.Cookie> cookies = CookieDecoder.decode(setCookieVal);
        Assert.assertEquals("No cookies returned.", 1, cookies.size());
        io.netty.handler.codec.http.Cookie cookie = cookies.iterator().next();
        Assert.assertEquals("Unexpected cookie name", cookie1Name, cookie.getName());
        Assert.assertEquals("Unexpected cookie value", cookie1val, cookie.getValue());
        Assert.assertEquals("Unexpected cookie domain", domain, cookie.getDomain());
        Assert.assertEquals("Unexpected cookie comment", comment, cookie.getComment());
        Assert.assertEquals("Unexpected cookie path", path, cookie.getPath());
        Assert.assertEquals("Unexpected cookie max age", age, cookie.getMaxAge());
    }

    @Test
    public void testSendErrorWithMsg() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        String errMsg = "What a mess!";
        servletResponse.sendError(500, errMsg);
        Assert.assertEquals("Error code not set in response.", 500, servletResponse.serverResponse().getStatus().code());
        byte[] content = new byte[servletResponse.getOutputWriterBuffer().readableBytes()];
        servletResponse.getOutputWriterBuffer().readBytes(content);

        Assert.assertEquals("Unexpected content length.", errMsg,
                            new String(content, servletResponse.getCharacterEncoding()));
    }

    @Test
    public void testSendErrorNoMsg() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        servletResponse.sendError(500);
        Assert.assertEquals("Error code not set in response.", 500, servletResponse.serverResponse().getStatus().code());
        Assert.assertNull("Content present for error, when not set.", servletResponse.getOutputWriterBuffer());
    }

    @Test
    public void testRedirectRelative() throws Exception {
        final String testUri = CONTEXT_PATH + SERVLET_PATH + REMAINING_PATH;
        HttpServletResponseImpl servletResponse = createServletResponse(testUri, null);
        String redirectUri = "c/d/e.html?a=b";
        servletResponse.sendRedirect(redirectUri);
        String locationHeader = servletResponse.serverResponse().getHeaders().get(HttpHeaders.Names.LOCATION);
        Assert.assertNotNull("Location header for relative redirect not present.", locationHeader);
        String expectedLocation = "http://" + LOCAL_ADDRESS + ':' + SERVER_PORT + testUri + '/' + redirectUri;
        Assert.assertEquals("Unexpected location header value.", expectedLocation, locationHeader);
    }

    @Test
    public void testRedirectAbsolute() throws Exception {
        final String testUri = CONTEXT_PATH + SERVLET_PATH + REMAINING_PATH;
        HttpServletResponseImpl servletResponse = createServletResponse(testUri, null);
        String redirectUri = "/c/d/e.html?a=b";
        servletResponse.sendRedirect(redirectUri);
        String locationHeader = servletResponse.serverResponse().getHeaders().get(HttpHeaders.Names.LOCATION);
        Assert.assertNotNull("Location header for relative redirect not present.", locationHeader);
        String expectedLocation = "http://" + LOCAL_ADDRESS + ':' + SERVER_PORT +
                                  CONTEXT_PATH.substring(0, CONTEXT_PATH.length() - 1) + redirectUri;
        Assert.assertEquals("Unexpected location header value.", expectedLocation, locationHeader);
    }

    @Test
    public void testAddDateHeader() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        String headerName = "MyDateHeader";

        Date now = new Date();
        long timeInMillis = now.getTime();

        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        String nowAsStr = dateFormatGmt.format(now);

        servletResponse.addDateHeader(headerName, timeInMillis);
        String dateHeaderVal = servletResponse.serverResponse().getHeaders().get(headerName);
        Assert.assertEquals("Date header set does not match the returned value in seconds.", nowAsStr, dateHeaderVal);

        servletResponse.setDateHeader(headerName, 0);
        List<String> allVals = servletResponse.serverResponse().getHeaders().getAll(headerName);
        Assert.assertEquals("Set header did not remove the previous value.", 1, allVals.size());
        Assert.assertEquals("Unexpected date value after reset.", dateFormatGmt.format(0), allVals.get(0));
    }

    @Test
    public void testHeader() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        String headerName = "MyHeader";
        String val1 = "val1";
        String val2 = "val2";
        servletResponse.setHeader(headerName, val1);
        String headerVal1 = servletResponse.serverResponse().getHeaders().get(headerName);
        Assert.assertEquals("Header value not set.", val1, headerVal1);

        servletResponse.addHeader(headerName, val2);
        List<String> allVals = servletResponse.serverResponse().getHeaders().getAll(headerName);
        Assert.assertEquals("Header must have two value.", 2, allVals.size());
        Assert.assertEquals("Unexpected first header value.", val1, allVals.get(0));
        Assert.assertEquals("Unexpected second header value.", val2, allVals.get(1));

    }

    @Test
    public void testIntHeader() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        String headerName = "MyIntHeader";
        String val1 = "1";
        String val2 = "2";
        servletResponse.setIntHeader(headerName, Integer.parseInt(val1));
        String headerVal1 = servletResponse.serverResponse().getHeaders().get(headerName);
        Assert.assertEquals("Header value not set.", val1, headerVal1);

        servletResponse.addIntHeader(headerName, Integer.parseInt(val2));
        List<String> allVals = servletResponse.serverResponse().getHeaders().getAll(headerName);
        Assert.assertEquals("Header must have two value.", 2, allVals.size());
        Assert.assertEquals("Unexpected first header value.", val1, allVals.get(0));
        Assert.assertEquals("Unexpected second header value.", val2, allVals.get(1));
    }

    @Test
    public void testCharEncoding() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        String characterEncoding = servletResponse.getCharacterEncoding();
        final String defaultEncoding = "ISO-8859-1";
        Assert.assertEquals("Unexpected default characted encoding for response.", defaultEncoding, characterEncoding);

        String encodingExpected = "UTF-8";
        servletResponse.setContentType("text/html;charset=" + encodingExpected);
        characterEncoding = servletResponse.getCharacterEncoding();
        Assert.assertEquals("Char encoding set via content type not honored.", encodingExpected, characterEncoding);

        servletResponse.setContentType("text/html");
        characterEncoding = servletResponse.getCharacterEncoding();
        Assert.assertEquals("Char encoding set via content type not honored.", defaultEncoding, characterEncoding);
    }

    @Test
    public void testOutputStream() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();

        String contentStr = "Hey I am content";
        servletResponse.getOutputStream().print(contentStr);
        servletResponse.flushBuffer();

        HttpResponseStatus status = servletResponse.serverResponse().getStatus();
        Assert.assertNotNull("Response status is null.", status);
        Assert.assertEquals("Unexpected response status.", 200, status.code());
        ByteBuf content = servletResponse.getOutputStreamBuffer();
        Assert.assertTrue("No response content written.", content.readableBytes() > 0);
        byte[] contentBytesWritten = new byte[content.readableBytes()];
        content.readBytes(contentBytesWritten);
        String contentWritten = new String(contentBytesWritten);

        Assert.assertEquals("Unexpected content.", contentStr, contentWritten);
    }

    @Test
    public void testWriter() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        String contentStr = "Hey I am content";
        servletResponse.getWriter().write(contentStr);
        servletResponse.flushBuffer();

        HttpResponseStatus status = servletResponse.serverResponse().getStatus();
        Assert.assertNotNull("Response status is null.", status);
        Assert.assertEquals("Unexpected response status.", 200, status.code());
        ByteBuf content = servletResponse.getOutputWriterBuffer();
        Assert.assertTrue("No response content written.", content.readableBytes() > 0);
        byte[] contentBytesWritten = new byte[content.readableBytes()];
        content.readBytes(contentBytesWritten);
        String contentWritten = new String(contentBytesWritten);

        Assert.assertEquals("Unexpected content.", contentStr, contentWritten);
    }

    @Test
    public void testResponseDefaults() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        String expectedContentType = "text/html; charset=utf-8";
        servletResponse.setContentType(expectedContentType); // set char encoding
        servletResponse.servletRequest().getSession(true); // create set-cookie header
        servletResponse.flushBuffer(); // sends response

        HttpResponseHeaders headers = servletResponse.serverResponse().getHeaders();
        String contentType = headers.get(HttpHeaders.Names.CONTENT_TYPE);
        Assert.assertEquals("Unexpected response content type.", expectedContentType, contentType);
        List<String> allCookieHeaders = headers.getAll(HttpHeaders.Names.SET_COOKIE);
        Assert.assertNotNull("Session id cookie not set. No cookies found.", allCookieHeaders);
        boolean found = false;
        for (String aCookieHeader : allCookieHeaders) {
            if (aCookieHeader.contains(JSESSIONID_COOKIE_NAME)) {
                found = true;
                break;
            }
        }

        Assert.assertTrue("JSESSIONID cookie not set.", found);
    }

    @Test
    public void testSetStatus() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        int expectedStatus = 299;
        servletResponse.setStatus(expectedStatus);

        Assert.assertEquals("Unexpected response status.", expectedStatus,
                            servletResponse.serverResponse().getStatus().code());
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleResponseSend() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        servletResponse.flushBuffer();
        servletResponse.flushBuffer();
    }

    @Test
    public void testEmptyContent() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        servletResponse.flushBuffer();
        Assert.assertEquals("Unexpected response status code for empty response.", HttpResponseStatus.OK.code(),
                            servletResponse.serverResponse().getStatus().code());
    }

    @Test
    public void testEmptyContentWithUpdatedStatus() throws Exception {
        HttpServletResponseImpl servletResponse = createServletResponse();
        int expectedCode = 200;
        servletResponse.setStatus(expectedCode);
        servletResponse.flushBuffer();
        Assert.assertEquals("Explicit response status code overridden on send.", expectedCode,
                            servletResponse.serverResponse().getStatus().code());
    }

    private static HttpServletResponseImpl createServletResponse() throws ExecutionException, InterruptedException {
        return createServletResponse(testUri, null);
    }

    private static HttpServletResponseImpl createServletResponse(String testUri,
                                                                 @Nullable HttpSessionManager sessionManager)
            throws ExecutionException, InterruptedException {
        sessionManager = null == sessionManager ? new HttpSessionManager(600) : sessionManager;
        DefaultFullHttpRequest nettyRequest = new DefaultFullHttpRequest(HTTP_VERSION, HTTP_METHOD, testUri);
        QueryStringDecoder decoder = new QueryStringDecoder(nettyRequest.getUri());
        HttpServletRequestImpl.PathComponents pathComponents =
                new HttpServletRequestImpl.PathComponents(decoder, CONTEXT_PATH, SERVLET_PATH);
        MockChannelHandlerContext context = new MockChannelHandlerContext(LOCAL_ADDRESS, SERVER_PORT,
                                                                          LOCAL_ADDRESS, LOCAL_PORT,
                                                                          REMOTE_ADDRESS, REMOTE_PORT);
        HttpServerRequest<ByteBuf> rxRequest = new HttpServerRequest<ByteBuf>(nettyRequest,
                                                                              PublishSubject.<ByteBuf>create());
        HttpServletRequestImpl request = new HttpServletRequestImpl(pathComponents, rxRequest, sessionManager,
                                                                    context, false);
        return new HttpServletResponseImpl(new HttpServerResponse<ByteBuf>(context, HTTP_VERSION,
                                                                           new MetricEventsSubject<ServerMetricsEvent<?>>()) { }, request);
    }

}
