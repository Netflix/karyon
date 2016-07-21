package netflix.karyon;

import com.netflix.governator.guice.BootstrapModule;

/**
 * An implementation of {@link KaryonServer} which wraps an existing {@link KaryonServer}.
 *
 * @author Nitesh Kant
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Deprecated
class KaryonServerBackedServer implements KaryonServer {

    private final AbstractKaryonServer delegate;
    private final BootstrapModule[] bootstrapModules;

    KaryonServerBackedServer(AbstractKaryonServer delegate, BootstrapModule... bootstrapModules) {
        this.delegate = delegate;
        this.bootstrapModules = bootstrapModules;
    }

    @Override
    public void start() {
        delegate.startWithAdditionalBootstrapModules(bootstrapModules);
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public void waitTillShutdown() {
        delegate.waitTillShutdown();
    }

    @Override
    public void startAndWaitTillShutdown() {
        start();
        waitTillShutdown();
    }

}
