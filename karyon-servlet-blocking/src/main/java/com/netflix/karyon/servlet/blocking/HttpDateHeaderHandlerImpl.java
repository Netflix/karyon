package com.netflix.karyon.servlet.blocking;

import io.reactivex.netty.protocol.http.server.HttpRequestHeaders;
import org.simpleframework.http.parse.DateParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Nitesh Kant
 */
public class HttpDateHeaderHandlerImpl implements HttpDateHeaderHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpDateHeaderHandlerImpl.class);

    /**
     * Uses {@link DateParser} to parse the header value to a date.
     *
     * @param headers All headers.
     * @param headerName Name of the header, value of which has to be returned.
     *
     * @return The date as a long, if exists, else -1.
     * @throws IllegalArgumentException if the header can not be converted to a date.
     */
    @Override
    public long getDateAsMillisFromEpoch(HttpRequestHeaders headers, String headerName) {
        String headerVal = headers.get(headerName);
        if (null != headerVal) {
            DateParser dateParser = new DateParser(headerVal);
            long dateVal = dateParser.toLong();
            if (dateVal <= 0) {
                IllegalArgumentException toThrow =
                        new IllegalArgumentException("Header " + headerName + " can not be converted to a date.");
                logger.error(String.format("Header %s can not be converted to a date. Value: %s , value as returned by parser %s",
                             headerName, headerVal, dateVal), toThrow);
                throw toThrow;
            }
            return dateVal;
        }
        return -1;
    }

    @Override
    public String convertToDateHeader(long millisSinceEpoch) {
        SimpleDateFormat sdf = getDateFormat();
        Date date = new Date(millisSinceEpoch);
        return sdf.format(date);
    }

    private static SimpleDateFormat getDateFormat() {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormatGmt;
    }
}
