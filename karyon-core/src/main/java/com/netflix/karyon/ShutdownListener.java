package com.netflix.karyon;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import io.reactivex.netty.protocol.udp.server.UdpServer;
import io.reactivex.netty.server.RxServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import java.nio.charset.Charset;


/**
 * A shutdown listener for karyon which aids shutdown of a server using a remote command over a socket. <br/>
 *
 * @author Nitesh Kant
 */
public class ShutdownListener {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownListener.class);

    private UdpServer<DatagramPacket, DatagramPacket> server;

    public ShutdownListener(int shutdownPort, final Func1<String, Observable<Void>> commandHandler) {
        server = RxNetty.createUdpServer(shutdownPort, new ConnectionHandler<DatagramPacket, DatagramPacket>() {
            @Override
            public Observable<Void> handle(ObservableConnection<DatagramPacket, DatagramPacket> newConnection) {
                return newConnection.getInput().map(new Func1<DatagramPacket, String>() {
                    @Override
                    public String call(DatagramPacket datagramPacket) {
                        String command = datagramPacket.content().toString(Charset.defaultCharset());
                        logger.info("Received a shutdown command: " + command);
                        return command;
                    }
                }).flatMap(commandHandler);
            }
        });
    }

    public ShutdownListener(int shutdownPort, final RxServer<?, ?> server) {
        this(shutdownPort, new Func1<String, Observable<Void>>() {
            @Override
            public Observable<Void> call(String cmd) {
                if ("shutdown".equalsIgnoreCase(cmd)) {
                    return shutdownAsync(server);
                }
                return Observable.error(new UnsupportedOperationException("Unknown command: " + cmd));
            }
        });
    }

    public void start() {
        server.start();
    }

    private static Observable<Void> shutdownAsync(final RxServer<?, ?> server) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    server.shutdown();
                    server.waitTillShutdown();
                    subscriber.onCompleted();
                } catch (InterruptedException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        ShutdownListener listener = new ShutdownListener(8888, RxNetty.createHttpServer(8877, new RequestHandler<ByteBuf, ByteBuf>() {
            @Override
            public Observable<Void> handle(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
                response.setStatus(HttpResponseStatus.NOT_FOUND);
                return response.close();
            }
        }));
        listener.start();
        listener.server.waitTillShutdown();
    }
}
