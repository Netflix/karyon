package netflix.karyon.examples.websockets;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.server.RxServer;
import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.Karyon;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusSuite;
import netflix.karyon.servo.KaryonServoModule;
import rx.Observable;
import rx.functions.Func1;

/**
 * Simple WebSockets echo server.
 *
 * @author Tomasz Bak
 */
public final class WebSocketEchoServer {

    public static void main(final String[] args) {
        RxServer<TextWebSocketFrame, TextWebSocketFrame> webSocketServer = RxNetty.newWebSocketServerBuilder(
                8888,
                new ConnectionHandler<TextWebSocketFrame, TextWebSocketFrame>() {
                    @Override
                    public Observable<Void> handle(final ObservableConnection<TextWebSocketFrame, TextWebSocketFrame> connection) {
                        return connection.getInput().flatMap(new Func1<WebSocketFrame, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(WebSocketFrame wsFrame) {
                                TextWebSocketFrame textFrame = (TextWebSocketFrame) wsFrame;
                                System.out.println("Got message: " + textFrame.text());
                                return connection.writeAndFlush(new TextWebSocketFrame(textFrame.text().toUpperCase()));
                            }
                        });
                    }
                }
        ).build();
        Karyon.forWebSocketServer(
                webSocketServer,
                new ArchaiusSuite("websocket-echo-server"),
                // KaryonEurekaModule.asSuite(), /* Uncomment if you need eureka */
                KaryonWebAdminModule.asSuite(),
                ShutdownModule.asSuite(),
                KaryonServoModule.asSuite())
                .startAndWaitTillShutdown();
    }
}
