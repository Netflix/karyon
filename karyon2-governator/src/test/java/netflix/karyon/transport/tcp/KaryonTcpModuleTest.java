package netflix.karyon.transport.tcp;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageCodec;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.pipeline.PipelineConfigurator;
import io.reactivex.netty.server.RxServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.functions.Func1;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Tomasz Bak
 */
public class KaryonTcpModuleTest {

    private static final Key<Map<String, RxServer>> RX_SERVERS_KEY = Key.get(new TypeLiteral<Map<String, RxServer>>() {
    });
    private static final String SERVER_MESSAGE = "Hello";

    private Injector injector;
    private LifecycleManager lifecycleManager;
    private RxServer server;

    @Before
    public void setUp() throws Exception {
        injector = LifecycleInjector.bootstrap(TestableTcpModule.class);
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
        String message = RxNetty.createTcpClient("localhost", server.getServerPort()).connect()
                .flatMap(new Func1<ObservableConnection<ByteBuf, ByteBuf>, Observable<String>>() {
                    @Override
                    public Observable<String> call(ObservableConnection<ByteBuf, ByteBuf> connection) {
                        return connection.getInput().map(new Func1<ByteBuf, String>() {
                            @Override
                            public String call(ByteBuf byteBuf) {
                                return byteBuf.toString(Charset.defaultCharset());
                            }
                        });
                    }
                }).single().toBlocking().toFuture().get(60, TimeUnit.SECONDS);

        assertEquals("Invalid message received from server", SERVER_MESSAGE, message);
    }

    public static class TestableTcpModule extends KaryonTcpModule<String, String> {
        public TestableTcpModule() {
            super("testTcpModule", String.class, String.class);
        }

        @Override
        protected void configureServer() {
            bindPipelineConfigurator().to(StringCodecPipelineConfigurator.class);
            bindConnectionHandler().to(TestableConnectionHandler.class);
            server().port(0);
        }
    }

    private static class TestableConnectionHandler implements ConnectionHandler<String, String> {
        @Override
        public Observable<Void> handle(ObservableConnection<String, String> connection) {
            return connection.writeAndFlush(SERVER_MESSAGE);
        }
    }

    public static class StringCodecPipelineConfigurator implements PipelineConfigurator<ByteBuf, String> {
        @Override
        public void configureNewPipeline(ChannelPipeline pipeline) {
            pipeline.addLast(new MessageToMessageCodec<ByteBuf, String>() {
                @Override
                protected void encode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
                    out.add(Unpooled.copiedBuffer(msg, Charset.defaultCharset()));
                }

                @Override
                protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
                    out.add(msg.toString(Charset.defaultCharset()));
                }
            });
        }
    }
}