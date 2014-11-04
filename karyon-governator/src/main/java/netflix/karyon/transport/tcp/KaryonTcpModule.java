package netflix.karyon.transport.tcp;

import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.RxServer;
import netflix.karyon.transport.AbstractServerModule;
import netflix.karyon.transport.AbstractServerModule.ServerConfigBuilder;

import static netflix.karyon.utils.TypeUtils.keyFor;

/**
 * @author Tomasz Bak
 */
@SuppressWarnings("rawtypes")
public abstract class KaryonTcpModule<I, O> extends AbstractServerModule<I, O, ServerConfigBuilder> {

    protected final Key<ConnectionHandler<I, O>> connectionHandlerKey;
    protected final Key<RxServer<I, O>> serverKey;


    protected KaryonTcpModule(String moduleName, Class<I> iType, Class<O> oType) {
        super(moduleName, iType, oType);
        connectionHandlerKey = keyFor(ConnectionHandler.class, iType, oType, nameAnnotation);
        serverKey = keyFor(RxServer.class, iType, oType, nameAnnotation);
    }

    @Override
    protected void configure() {
        configureServer();
        bind(serverConfigKey).toInstance(serverConfigBuilder.build());
        MapBinder.newMapBinder(binder(), String.class, RxServer.class).addBinding(nameAnnotation.value()).toProvider(
                new TcpRxServerProvider<I, O, RxServer<I, O>>(nameAnnotation.value(), iType, oType)
        ).asEagerSingleton();
    }

    @Override
    protected ServerConfigBuilder newServerConfigBuilder() {
        return new ServerConfigBuilder();
    }

    public LinkedBindingBuilder<ConnectionHandler<I, O>> bindConnectionHandler() {
        return bind(connectionHandlerKey);
    }
}
