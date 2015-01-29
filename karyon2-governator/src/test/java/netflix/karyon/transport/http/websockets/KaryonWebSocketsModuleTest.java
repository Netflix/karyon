package netflix.karyon.transport.http.websockets;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.server.RxServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.functions.Func1;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Tomasz Bak
 */
public class KaryonWebSocketsModuleTest {
    private static final Key<Map<String, RxServer>> RX_SERVERS_KEY = Key.get(new TypeLiteral<Map<String, RxServer>>() {
    });
    private static final String SERVER_MESSAGE = "Hello";

    private Injector injector;
    private LifecycleManager lifecycleManager;
    private RxServer server;

    @Before
    public void setUp() throws Exception {
        injector = LifecycleInjector.bootstrap(TestableWebSocketsModule.class);
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
    public void testGovernatedTcpServer() throws Exception {
        String message = RxNetty.<TextWebSocketFrame, TextWebSocketFrame>newWebSocketClientBuilder("localhost", server.getServerPort())
                .build()
                .connect()
                .flatMap(new Func1<ObservableConnection<TextWebSocketFrame, TextWebSocketFrame>, Observable<String>>() {
                    @Override
                    public Observable<String> call(ObservableConnection<TextWebSocketFrame, TextWebSocketFrame> connection) {
                        return connection.getInput().map(new Func1<TextWebSocketFrame, String>() {
                            @Override
                            public String call(TextWebSocketFrame frame) {
                                return frame.text();
                            }
                        });
                    }
                }).single().toBlocking().toFuture().get(60, TimeUnit.SECONDS);

        assertEquals("Invalid message received from server", SERVER_MESSAGE, message);
    }

    public static class TestableWebSocketsModule extends
            KaryonWebSocketsModule<TextWebSocketFrame, TextWebSocketFrame> {
        public TestableWebSocketsModule() {
            super("testWebSocketsModule", TextWebSocketFrame.class, TextWebSocketFrame.class);
        }

        @Override
        protected void configureServer() {
            bindConnectionHandler().to(TestableConnectionHandler.class);
            server().port(0);
        }
    }

    private static class TestableConnectionHandler implements ConnectionHandler<TextWebSocketFrame, TextWebSocketFrame> {
        @Override
        public Observable<Void> handle(ObservableConnection<TextWebSocketFrame, TextWebSocketFrame> newConnection) {
            return newConnection.writeAndFlush(new TextWebSocketFrame(SERVER_MESSAGE));
        }
    }
}
