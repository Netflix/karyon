package netflix.karyon;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.annotations.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import io.reactivex.netty.protocol.udp.server.UdpServer;
import io.reactivex.netty.server.RxServer;
import netflix.karyon.transport.KaryonTransport;
import netflix.karyon.transport.http.HttpRequestHandler;
import rx.Observable;

/**
 * An entry point into karyon to create various flavors of karyon servers. For applications using governator's
 * {@link Bootstrap} annotations it is easier to use {@link KaryonRunner} passing the main class containing all the
 * {@link Bootstrap} annotations.
 *
 * @author Nitesh Kant
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Deprecated
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
        return forRequestHandler(port, handler, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} that has a single HTTP server instance which delegates all request
     * handling to {@link RequestHandler}.
     * The {@link HttpServer} is created using {@link KaryonTransport#newHttpServer(int, HttpRequestHandler)}
     *
     * @param port Port for the server.
     * @param handler Request Handler
     * @param bootstrapModules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forRequestHandler(int port, final RequestHandler<ByteBuf, ByteBuf> handler,
                                                 BootstrapModule... bootstrapModules) {
        HttpServer<ByteBuf, ByteBuf> httpServer =
                KaryonTransport.newHttpServer(port, new RequestHandler<ByteBuf, ByteBuf>() {
                    @Override
                    public Observable<Void> handle(HttpServerRequest<ByteBuf> request,
                                                   HttpServerResponse<ByteBuf> response) {
                        return handler.handle(request, response);
                    }
                });
        return new RxNettyServerBackedServer(httpServer, bootstrapModules);
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
        return forTcpConnectionHandler(port, handler, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} that has a single TCP server instance which delegates all connection
     * handling to {@link ConnectionHandler}.
     * The {@link RxServer} is created using {@link RxNetty#newTcpServerBuilder(int, ConnectionHandler)}
     *
     * @param port Port for the server.
     * @param handler Connection Handler
     * @param bootstrapModules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forTcpConnectionHandler(int port, ConnectionHandler<ByteBuf, ByteBuf> handler,
                                                       BootstrapModule... bootstrapModules) {
        RxServer<ByteBuf, ByteBuf> server = RxNetty.newTcpServerBuilder(port, handler).build();
        return new RxNettyServerBackedServer(server, bootstrapModules);
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
        return forUdpConnectionHandler(port, handler, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} that has a single UDP server instance which delegates all connection
     * handling to {@link ConnectionHandler}.
     * The {@link RxServer} is created using {@link RxNetty#newUdpServerBuilder(int, ConnectionHandler)}
     *
     * @param port Port for the server.
     * @param handler Connection Handler
     * @param bootstrapModules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forUdpConnectionHandler(int port, ConnectionHandler<ByteBuf, ByteBuf> handler,
                                                       BootstrapModule... bootstrapModules) {
        UdpServer<ByteBuf, ByteBuf> server = RxNetty.newUdpServerBuilder(port, handler).build();
        return new RxNettyServerBackedServer(server, bootstrapModules);
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
        return forTcpServer(server, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link RxServer} with
     * it's own lifecycle.
     *
     * @param server TCP server
     * @param bootstrapModules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forTcpServer(RxServer<?, ?> server, BootstrapModule... bootstrapModules) {
        return new RxNettyServerBackedServer(server, bootstrapModules);
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link HttpServer} with
     * it's own lifecycle.
     *
     * @param server HTTP server
     * @param modules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forHttpServer(HttpServer<?, ?> server, Module... modules) {
        return forHttpServer(server, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link HttpServer} with
     * it's own lifecycle.
     *
     * @param server HTTP server
     * @param bootstrapModules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forHttpServer(HttpServer<?, ?> server,
                                             BootstrapModule... bootstrapModules) {
        return new RxNettyServerBackedServer(server, bootstrapModules);
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed WebSockets {@link RxServer} with
     * it's own lifecycle.
     *
     * @param server WebSocket server
     * @param modules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forWebSocketServer(RxServer<? extends WebSocketFrame, ? extends WebSocketFrame> server, Module... modules) {
        return forWebSocketServer(server, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed WebSockets {@link RxServer} with
     * it's own lifecycle.
     *
     * @param server WebSocket server
     * @param bootstrapModules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forWebSocketServer(RxServer<? extends WebSocketFrame, ? extends WebSocketFrame> server,
                                                  BootstrapModule... bootstrapModules) {
        return new RxNettyServerBackedServer(server, bootstrapModules);
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link UdpServer} with
     * it's own lifecycle.
     *
     * @param server UDP server
     * @param modules Additional modules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forUdpServer(UdpServer<?, ?> server, Module... modules) {
        return forUdpServer(server, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link UdpServer} with
     * it's own lifecycle.
     *
     * @param server UDP server
     * @param bootstrapModules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forUdpServer(UdpServer<?, ?> server, BootstrapModule... bootstrapModules) {
        return new RxNettyServerBackedServer(server, bootstrapModules);
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
        return forServer(server, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which combines lifecycle of the passed {@link KaryonServer} with
     * it's own lifecycle. This is useful when a {@link KaryonServer} is already present and the passed
     * {@link BootstrapModule}s are to be added to that server.
     *
     * @param server An existing karyon server
     * @param bootstrapModules Additional bootstrapModules.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forServer(KaryonServer server, BootstrapModule... bootstrapModules) {
        return new KaryonServerBackedServer((AbstractKaryonServer)server, bootstrapModules);
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
        return forApplication(mainClass, toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} which uses the passed class to detect any modules.
     *
     * @param mainClass Any class/interface containing governator's {@link Bootstrap} annotations.
     * @param bootstrapModules Additional bootstrapModules if any.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forApplication(Class<?> mainClass, BootstrapModule... bootstrapModules) {
        return new MainClassBasedServer(mainClass, bootstrapModules);
    }

    /**
     * Creates a new {@link KaryonServer} from the passed modules.
     *
     * @param modules Modules to use for the server.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forModules(Module... modules) {
        return forSuites(toBootstrapModule(modules));
    }

    /**
     * Creates a new {@link KaryonServer} from the passed bootstrapModules.
     *
     * @param bootstrapModules Bootstrap modules to use for the server.
     *
     * @return {@link KaryonServer} which is to be used to start the created server.
     */
    public static KaryonServer forSuites(BootstrapModule... bootstrapModules) {
        return new MainClassBasedServer(KaryonServer.class, bootstrapModules);
    }

    public static BootstrapModule toBootstrapModule(final Module... modules) {
        if (null == modules) {
            return null;
        }
        return new BootstrapModule() {
            @Override
            public void configure(BootstrapBinder binder) {
                binder.includeModules(modules);
            }

        };
    }

    @SafeVarargs
    public static BootstrapModule toBootstrapModule(final Class<? extends Module>... moduleClasses) {
        if (null == moduleClasses) {
            return null;
        }
        return new BootstrapModule() {
            @Override
            public void configure(BootstrapBinder binder) {
                binder.include(Lists.newArrayList(moduleClasses));
            }

        };
    }

    public static BootstrapModule toBootstrapModule(final Class<? extends Module> moduleClass) {
        if (null == moduleClass) {
            return null;
        }
        return new BootstrapModule() {
            @Override
            public void configure(BootstrapBinder binder) {
                binder.include(moduleClass);
            }

        };
    }
}
