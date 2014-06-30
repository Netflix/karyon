package com.netflix.hellonoss.server;

import com.netflix.karyon.transport.interceptor.DuplexInterceptor;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
class LoggingInterceptor implements DuplexInterceptor<ByteBuf, ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static int count;
    private final int id;

    LoggingInterceptor() {
        id = ++count;
    }

    @Override
    public Observable<Void> in(ByteBuf request, ByteBuf response) {
        logger.info("Logging interceptor with id {} invoked for direction IN.", id);
        return Observable.empty();
    }

    @Override
    public Observable<Void> out(ByteBuf response) {
        logger.info("Logging interceptor with id {} invoked for direction OUT.", id);
        return Observable.empty();
    }
}
