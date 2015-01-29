package netflix.karyon.transport.http;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import io.reactivex.netty.server.RxServer;
import netflix.karyon.transport.interceptor.DuplexInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * @author Tomasz Bak
 */
public class KaryonHttpModuleTest {

    private static final Key<Map<String, RxServer>> RX_SERVERS_KEY = Key.get(new TypeLiteral<Map<String, RxServer>>() {
    });

    private Injector injector;
    private LifecycleManager lifecycleManager;
    private RxServer server;

    @Before
    public void setUp() throws Exception {
        injector = LifecycleInjector.bootstrap(TestableHttpModule.class);
        lifecycleManager = injector.getInstance(LifecycleManager.class);
        lifecycleManager.start();
        server = injector.getInstance(RX_SERVERS_KEY).values().iterator().next();
    }

    @After
    public void tearDown() throws Exception {
        if (lifecycleManager != null) {
            lifecycleManager.close();
        }
    }

    @Test
    public void testHttpRouterAndInterceptorSupport() throws Exception {
        int counterInitial = CountingInterceptor.counter.get();

        HttpResponseStatus status = sendRequest("/sendOK", server);
        assertEquals("Expected HTTP 200", HttpResponseStatus.OK, status);

        status = sendRequest("/sendNotFound", server);
        assertEquals("Expected HTTP NOT_FOUND", HttpResponseStatus.NOT_FOUND, status);

        int counterFinal = CountingInterceptor.counter.get();
        assertEquals("Invalid number of counting interceptor invocations", 4, counterFinal - counterInitial);
    }

    private HttpResponseStatus sendRequest(String path, RxServer server) throws Exception {
        return (HttpResponseStatus) RxNetty.createHttpGet("http://localhost:" + server.getServerPort() + path)
                .flatMap(new Func1<HttpClientResponse<ByteBuf>, Observable<?>>() {
                    @Override
                    public Observable<HttpResponseStatus> call(HttpClientResponse<ByteBuf> httpClientResponse) {
                        return Observable.just(httpClientResponse.getStatus());
                    }
                }).single().toBlocking().toFuture().get(60, TimeUnit.SECONDS);
    }

    public static class TestableRequestRouter implements RequestHandler<ByteBuf, ByteBuf> {

        @Override
        public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
            if (request.getPath().contains("/sendOK")) {
                response.setStatus(HttpResponseStatus.OK);
            } else if (request.getPath().contains("/sendNotFound")) {
                response.setStatus(HttpResponseStatus.NOT_FOUND);
            } else {
                response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
            return Observable.empty();
        }
    }

    public static class TestableHttpModule extends KaryonHttpModule<ByteBuf, ByteBuf> {
        public TestableHttpModule() {
            super("testableHttpModule", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindRouter().to(TestableRequestRouter.class);
            interceptorSupport().forHttpMethod(HttpMethod.GET).intercept(CountingInterceptor.class);
            server().port(0);
        }
    }

    public static class CountingInterceptor implements DuplexInterceptor<HttpServerRequest<ByteBuf>, HttpServerResponse<ByteBuf>> {

        static AtomicInteger counter = new AtomicInteger();

        @Override
        public Observable<Void> in(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
            counter.incrementAndGet();
            return Observable.empty();
        }

        @Override
        public Observable<Void> out(HttpServerResponse<ByteBuf> response) {
            counter.incrementAndGet();
            return Observable.empty();
        }
    }
}