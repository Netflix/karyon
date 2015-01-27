package netflix.karyon.examples.hellonoss.common;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import netflix.karyon.transport.interceptor.DuplexInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * @author Nitesh Kant
 */
public class LoggingInterceptor implements DuplexInterceptor<HttpServerRequest<ByteBuf>, HttpServerResponse<ByteBuf>> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    private static int count;
    private final int id;

    public LoggingInterceptor() {
        id = ++count;
    }

    @Override
    public Observable<Void> in(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        logger.info("Logging interceptor with id {} invoked for direction IN.", id);
        return Observable.empty();
    }

    @Override
    public Observable<Void> out(HttpServerResponse<ByteBuf> response) {
        logger.info("Logging interceptor with id {} invoked for direction OUT.", id);
        return Observable.empty();
    }
}
