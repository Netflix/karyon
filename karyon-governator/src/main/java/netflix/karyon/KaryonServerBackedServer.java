package netflix.karyon;

import com.netflix.governator.guice.BootstrapModule;

/**
 * An implementation of {@link KaryonServer} which wraps an existing {@link KaryonServer}.
 *
 * @author Nitesh Kant
 */
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
