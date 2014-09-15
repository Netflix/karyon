package com.netflix.karyon;

import com.google.inject.Module;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.governator.guice.annotations.Bootstrap;
import com.netflix.karyon.transport.KaryonTransport;
import com.netflix.karyon.transport.http.HttpRequestHandler;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import io.reactivex.netty.protocol.udp.server.UdpServer;
import io.reactivex.netty.server.RxServer;
import rx.Observable;

/**
 * An entry point into karyon to create various flavors of karyon servers. For applications using governator's
 * {@link Bootstrap} annotations it is easier to use {@link KaryonRunner} passing the main class containing all the
 * {@link Bootstrap} annotations.
 *
 * @author Nitesh Kant
 */
public final class Karyon {

    private Karyon() {
    }

    /**
     * Creates a new {@link KaryonServer} that has a single HTTP server instance which delegates all request
     * handling to {@link RequestHandler}.
     * The {@link HttpServer} is created using {@link KaryonTransport#newHttpServer(int, HttpRequestHandler)}
     *
     * @param port Port for the server.
     * @param handler Request Handler
     * @param modules Additional modules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forRequestHandler(int port, final RequestHandler<ByteBuf, ByteBuf> handler,
                                                 Module... modules) {
        return forRequestHandler(port, handler, toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} that has a single HTTP server instance which delegates all request
     * handling to {@link RequestHandler}.
     * The {@link HttpServer} is created using {@link KaryonTransport#newHttpServer(int, HttpRequestHandler)}
     *
     * @param port Port for the server.
     * @param handler Request Handler
     * @param suites Additional suites if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forRequestHandler(int port, final RequestHandler<ByteBuf, ByteBuf> handler,
                                                 LifecycleInjectorBuilderSuite... suites) {
        HttpServer<ByteBuf, ByteBuf> httpServer =
                KaryonTransport.newHttpServer(port, new RequestHandler<ByteBuf, ByteBuf>() {
                    @Override
                    public Observable<Void> handle(HttpServerRequest<ByteBuf> request,
                                                   HttpServerResponse<ByteBuf> response) {
                        return handler.handle(request, response);
                    }
                });
        return new RxNettyServerBackedServer(httpServer, suites);
    }

    /**
     * Creates a new {@link KaryonServer} that has a single TCP server instance which delegates all connection
     * handling to {@link ConnectionHandler}.
     * The {@link RxServer} is created using {@link RxNetty#newTcpServerBuilder(int, ConnectionHandler)}
     *
     * @param port Port for the server.
     * @param handler Connection Handler
     * @param modules Additional modules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forTcpConnectionHandler(int port, ConnectionHandler<ByteBuf, ByteBuf> handler,
                                                       Module... modules) {
        return forTcpConnectionHandler(port, handler, toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} that has a single TCP server instance which delegates all connection
     * handling to {@link ConnectionHandler}.
     * The {@link RxServer} is created using {@link RxNetty#newTcpServerBuilder(int, ConnectionHandler)}
     *
     * @param port Port for the server.
     * @param handler Connection Handler
     * @param suites Additional suites if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forTcpConnectionHandler(int port, ConnectionHandler<ByteBuf, ByteBuf> handler,
                                                       LifecycleInjectorBuilderSuite... suites) {
        RxServer<ByteBuf, ByteBuf> server = RxNetty.newTcpServerBuilder(port, handler).build();
        return new RxNettyServerBackedServer(server, suites);
    }

    /**
     * Creates a new {@link KaryonServer} that has a single UDP server instance which delegates all connection
     * handling to {@link ConnectionHandler}.
     * The {@link RxServer} is created using {@link RxNetty#newUdpServerBuilder(int, ConnectionHandler)}
     *
     * @param port Port for the server.
     * @param handler Connection Handler
     * @param modules Additional modules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forUdpConnectionHandler(int port, ConnectionHandler<ByteBuf, ByteBuf> handler,
                                                       Module... modules) {
        return forUdpConnectionHandler(port, handler, toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} that has a single UDP server instance which delegates all connection
     * handling to {@link ConnectionHandler}.
     * The {@link RxServer} is created using {@link RxNetty#newUdpServerBuilder(int, ConnectionHandler)}
     *
     * @param port Port for the server.
     * @param handler Connection Handler
     * @param suites Additional suites if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forUdpConnectionHandler(int port, ConnectionHandler<ByteBuf, ByteBuf> handler,
                                                       LifecycleInjectorBuilderSuite... suites) {
        UdpServer<ByteBuf, ByteBuf> server = RxNetty.newUdpServerBuilder(port, handler).build();
        return new RxNettyServerBackedServer(server, suites);
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link RxServer} with
     * it's own lifecycle.
     *
     * @param server TCP server
     * @param modules Additional modules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forTcpServer(RxServer<?, ?> server, Module... modules) {
        return forTcpServer(server, toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link RxServer} with
     * it's own lifecycle.
     *
     * @param server TCP server
     * @param suites Additional suites if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forTcpServer(RxServer<?, ?> server, LifecycleInjectorBuilderSuite... suites) {
        return new RxNettyServerBackedServer(server, suites);
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link HttpServer} with
     * it's own lifecycle.
     *
     * @param server TCP server
     * @param modules Additional suites if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forHttpServer(HttpServer<?, ?> server, Module... modules) {
        return forHttpServer(server, toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link HttpServer} with
     * it's own lifecycle.
     *
     * @param server TCP server
     * @param suites Additional suites if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forHttpServer(HttpServer<?, ?> server,
                                             LifecycleInjectorBuilderSuite... suites) {
        return new RxNettyServerBackedServer(server, suites);
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link UdpServer} with
     * it's own lifecycle.
     *
     * @param server TCP server
     * @param modules Additional modules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forUdpServer(UdpServer<?, ?> server, Module... modules) {
        return forUdpServer(server, toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link UdpServer} with
     * it's own lifecycle.
     *
     * @param server TCP server
     * @param suites Additional suites if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forUdpServer(UdpServer<?, ?> server, LifecycleInjectorBuilderSuite... suites) {
        return new RxNettyServerBackedServer(server, suites);
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link KaryonServer} with
     * it's own lifecycle. This is useful when a {@link KaryonServer} is already present and the passed
     * {@link Module}s are to be added to that server.
     *
     * @param server An existing karyon server
     * @param modules Additional modules.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forServer(KaryonServer server, Module... modules) {
        return forServer(server, toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link KaryonServer} with
     * it's own lifecycle. This is useful when a {@link KaryonServer} is already present and the passed
     * {@link LifecycleInjectorBuilderSuite}s are to be added to that server.
     *
     * @param server An existing karyon server
     * @param suites Additional suites.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forServer(KaryonServer server, LifecycleInjectorBuilderSuite... suites) {
        return new KaryonServerBackedServer((AbstractKaryonServer)server, suites);
    }

    /**
     * Creates a new {@link KaryonServer} which uses the passed class to detect any modules.
     *
     * @param mainClass Any class/interface containing governator's {@link Bootstrap} annotations.
     * @param modules Additional modules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forApplication(Class<?> mainClass, Module... modules) {
        return forApplication(mainClass, toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which uses the passed class to detect any modules.
     *
     * @param mainClass Any class/interface containing governator's {@link Bootstrap} annotations.
     * @param suites Additional suites if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forApplication(Class<?> mainClass, LifecycleInjectorBuilderSuite... suites) {
        return new MainClassBasedServer(mainClass, suites);
    }

    /**
     * Creates a new {@link KaryonServer} from the passed modules.
     *
     * @param modules Modules to use for the server.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forModules(Module... modules) {
        return forSuites(toSuite(modules));
    }

    /**
     * Creates a new {@link KaryonServer} from the passed suites.
     *
     * @param suites Suites to use for the server.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forSuites(LifecycleInjectorBuilderSuite... suites) {
        return new MainClassBasedServer(KaryonServer.class, suites);
    }

    private static LifecycleInjectorBuilderSuite toSuite(final Module... modules) {
        if (null == modules) {
            return null;
        }
        return new LifecycleInjectorBuilderSuite() {
            @Override
            public void configure(LifecycleInjectorBuilder builder) {
                builder.withAdditionalModules(modules);
            }
        };
    }
}
