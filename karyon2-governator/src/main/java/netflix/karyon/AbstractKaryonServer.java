package netflix.karyon;

import com.google.inject.Injector;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.lifecycle.LifecycleManager;

import java.util.Arrays;

/**
 * @author Nitesh Kant
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Deprecated
abstract class AbstractKaryonServer implements KaryonServer {

    protected final BootstrapModule[] bootstrapModules;
    protected LifecycleManager lifecycleManager;
    protected Injector injector;

    public AbstractKaryonServer(BootstrapModule... bootstrapModules) {
        this.bootstrapModules = bootstrapModules;
    }

    @Override
    public final void start() {
        startWithAdditionalBootstrapModules();
    }

    public final void startWithAdditionalBootstrapModules(BootstrapModule... additionalBootstrapModules) {
        BootstrapModule[] applicableBootstrapModules = this.bootstrapModules;
        if (null != additionalBootstrapModules && additionalBootstrapModules.length != 0) {
            applicableBootstrapModules = Arrays.copyOf(bootstrapModules, bootstrapModules.length + additionalBootstrapModules.length);
            System.arraycopy(additionalBootstrapModules, 0, applicableBootstrapModules, bootstrapModules.length, additionalBootstrapModules.length);
        }

        injector = newInjector(applicableBootstrapModules);

        startLifecycleManager();
        _start();
    }

    protected abstract void _start();

    @Override
    public void shutdown() {
        if (lifecycleManager != null) {
            lifecycleManager.close();
        }
    }

    @Override
    public void startAndWaitTillShutdown() {
        start();
        waitTillShutdown();
    }

    protected abstract Injector newInjector(BootstrapModule... applicableBootstrapModules);

    protected void startLifecycleManager() {
        lifecycleManager = injector.getInstance(LifecycleManager.class);
        try {
            lifecycleManager.start();
        } catch (Exception e) {
            throw new RuntimeException(e); // So that this does not pollute the API.
        }
    }
}
