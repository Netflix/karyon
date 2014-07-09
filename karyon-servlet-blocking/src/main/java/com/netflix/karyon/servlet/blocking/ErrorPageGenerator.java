package com.netflix.karyon.servlet.blocking;

import io.netty.buffer.ByteBuf;

/**
 * An error page generator used for sending HTML responses for {@link HttpServletResponseImpl#sendError(int, String)}
 *
 * @author Nitesh Kant
 */
public interface ErrorPageGenerator {

    ByteBuf getErrorPage(int responseCode, String errorMsg);
}
