package netflix.karyon;

import com.netflix.governator.guice.BootstrapModule;
import io.reactivex.netty.server.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link KaryonServer} which wraps an RxNetty's server.
 *
 * @author Nitesh Kant
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Deprecated
class RxNettyServerBackedServer extends MainClassBasedServer {

    private static final Logger logger = LoggerFactory.getLogger(RxNettyServerBackedServer.class);

    @SuppressWarnings("rawtypes") private final AbstractServer rxNettyServer;

    RxNettyServerBackedServer(AbstractServer rxNettyServer, BootstrapModule... bootstrapModules) {
        super(RxNettyServerBackedServer.class, bootstrapModules);
        this.rxNettyServer = rxNettyServer;
    }

    @Override
    protected void _start() {
        rxNettyServer.start();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            rxNettyServer.shutdown();
        } catch (InterruptedException e) {
            logger.error("Interrupted while shutdown.", e);
            Thread.interrupted();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void waitTillShutdown() {
        try {
            rxNettyServer.waitTillShutdown();
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for shutdown.", e);
            Thread.interrupted();
            throw new RuntimeException(e);
        }
    }

}
