package com.netflix.karyon.server.http.servlet.blocking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

/**
 * @author Nitesh Kant
 */
public class DefaultErrorPage implements ErrorPageGenerator {

    @Override
    public ByteBuf getErrorPage(int responseCode, String errorMsg) {
        return Unpooled.copiedBuffer(errorMsg, Charset.defaultCharset()); // TODO: Create HTML
    }
}
