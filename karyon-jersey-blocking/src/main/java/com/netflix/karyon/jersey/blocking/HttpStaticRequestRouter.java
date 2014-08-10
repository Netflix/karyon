package com.netflix.karyon.jersey.blocking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import rx.Observable;

import com.google.common.net.HttpHeaders;
import com.netflix.karyon.transport.http.HttpRequestRouter;

public class HttpStaticRequestRouter implements HttpRequestRouter<ByteBuf, ByteBuf> {

    public static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    public static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final int HTTP_CACHE_SECONDS = 60;

    @Override
    public Observable<Void> route(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        if (!request.getHttpMethod().equals(HttpMethod.GET)) {
            return Observable.empty();
        }
        
        try {
            final String uri = request.getUri();
            final String path = sanitizeUri(uri);
            
            File file = new File(Thread.currentThread().getContextClassLoader().getResource(path).toURI());
            if (file.isHidden() || !file.exists()) {
                response.setStatus(HttpResponseStatus.NOT_FOUND);
                return Observable.empty();
            }

            if (file.isDirectory()) {
                // File listing not allowed
                response.setStatus(HttpResponseStatus.FORBIDDEN);
                return Observable.empty();
            }
            
            if (!file.isFile()) {
                response.setStatus(HttpResponseStatus.FORBIDDEN);
                return Observable.empty();
            }
            
            // Cache Validation
            String ifModifiedSince = request.getHeaders().get(IF_MODIFIED_SINCE);
            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
                Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

                // Only compare up to the second because the datetime format we send to the client
                // does not have milliseconds
                long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
                long fileLastModifiedSeconds = file.lastModified() / 1000;
                if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                    response.setStatus(HttpResponseStatus.NOT_MODIFIED);
                    setDateHeader(response);
                    return Observable.empty();
                }
            }
            
            
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            long fileLength = file.length();
            
            response.setStatus(HttpResponseStatus.OK);
            response.getHeaders().setContentLength(fileLength);
            setContentTypeHeader(response, file);
            setDateAndCacheHeaders(response, file);
//            if (HttpHeaderUtil.isKeepAlive(request)) {
//                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
//            }

            
            // Copy contents
            ByteBuf bb = Unpooled.buffer((int)fileLength); 
            bb.writeBytes(is, (int)fileLength);
            
            response.writeAndFlush(bb);
            
            return Observable.empty();
        }
        catch (Exception e) {
            return Observable.error(e);
        }
    }

    public static String sanitizeUri(String uri) throws Exception {
        // Decode the path.
        uri = URLDecoder.decode(uri, "UTF-8");

        if (!uri.startsWith("/")) {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
            uri.contains('.' + File.separator) ||
            uri.startsWith(".") || uri.endsWith(".") ||
            INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        // Convert to absolute path.
        return "WEB-INF" + uri;
    }
    
    /**
     * Sets the Date header for the HTTP response
     *
     * @param response
     *            HTTP response
     */
    private static void setDateHeader(HttpServerResponse<ByteBuf> response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.getHeaders().set(HttpHeaders.DATE, dateFormatter.format(time.getTime()));
    }
    
    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param fileToCache
     *            file to extract content type
     */
    public static void setDateAndCacheHeaders(HttpServerResponse<ByteBuf> response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.getHeaders().set(HttpHeaders.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.getHeaders().set(HttpHeaders.EXPIRES, dateFormatter.format(time.getTime()));
        response.getHeaders().set(HttpHeaders.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.getHeaders().set(HttpHeaders.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    public static void setContentTypeHeader(HttpServerResponse<ByteBuf> response, File file) {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
    }
}
