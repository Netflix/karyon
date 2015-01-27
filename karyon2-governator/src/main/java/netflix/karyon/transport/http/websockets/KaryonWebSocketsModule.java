package netflix.karyon.transport.http.websockets;

import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.RxServer;
import netflix.karyon.transport.AbstractServerModule;
import netflix.karyon.transport.http.websockets.KaryonWebSocketsModule.WebSocketsServerConfigBuilder;

import static netflix.karyon.utils.TypeUtils.keyFor;

/**
 * @author Tomasz Bak
 */
public abstract class KaryonWebSocketsModule<I extends WebSocketFrame, O extends WebSocketFrame> extends
        AbstractServerModule<I, O, WebSocketsServerConfigBuilder> {

    protected final Key<ConnectionHandler<I, O>> connectionHandlerKey;
    protected final Key<RxServer<I, O>> serverKey;


    protected KaryonWebSocketsModule(String moduleName, Class<I> iType, Class<O> oType) {
        super(moduleName, iType, oType);
        connectionHandlerKey = keyFor(ConnectionHandler.class, iType, oType, nameAnnotation);
        serverKey = keyFor(RxServer.class, iType, oType, nameAnnotation);
    }

    @Override
    protected void configure() {
        configureServer();
        bind(serverConfigKey).toInstance(serverConfigBuilder.build());
        MapBinder.newMapBinder(binder(), String.class, RxServer.class).addBinding(nameAnnotation.value()).toProvider(
                new WebSocketsRxServerProvider<I, O, RxServer<I, O>>(nameAnnotation.value(), iType, oType)
        ).asEagerSingleton();
    }

    @Override
    protected WebSocketsServerConfigBuilder newServerConfigBuilder() {
        return new WebSocketsServerConfigBuilder();
    }

    public LinkedBindingBuilder<ConnectionHandler<I, O>> bindConnectionHandler() {
        return bind(connectionHandlerKey);
    }

    public static class WebSocketsServerConfig extends AbstractServerModule.ServerConfig {
        private final boolean messageAggregator;

        public WebSocketsServerConfig(int port, boolean messageAggregator) {
            super(port);
            this.messageAggregator = messageAggregator;
        }

        public boolean isMessageAggregator() {
            return messageAggregator;
        }
    }

    public static class WebSocketsServerConfigBuilder extends AbstractServerModule.ServerConfigBuilder<WebSocketsServerConfigBuilder, WebSocketsServerConfig> {

        protected boolean messageAggregator;

        public WebSocketsServerConfigBuilder withMessageAggregator(boolean messageAggregator) {
            this.messageAggregator = messageAggregator;
            return this;
        }

        @Override
        public WebSocketsServerConfig build() {
            return new WebSocketsServerConfig(port, messageAggregator);
        }
    }
}
