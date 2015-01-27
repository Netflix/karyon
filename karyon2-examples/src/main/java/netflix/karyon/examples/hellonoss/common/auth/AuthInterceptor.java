package netflix.karyon.examples.hellonoss.common.auth;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import netflix.karyon.transport.interceptor.InboundInterceptor;
import rx.Observable;
import rx.functions.Func1;

import javax.inject.Inject;

/**
 * @author Nitesh Kant
 */
public class AuthInterceptor implements InboundInterceptor<HttpServerRequest<ByteBuf>, HttpServerResponse<ByteBuf>> {

    private final AuthenticationService authService;

    @Inject
    public AuthInterceptor(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public Observable<Void> in(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        return authService.authenticate(request).map(new Func1<Boolean, Void>() {
            @Override
            public Void call(Boolean aBoolean) {
                return null;
            }
        });
    }
}
