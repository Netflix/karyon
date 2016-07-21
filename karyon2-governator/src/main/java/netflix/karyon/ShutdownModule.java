package netflix.karyon;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.lifecycle.LifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action0;

import javax.annotation.PostConstruct;

/**
 * Provide shutdown listener as Governator managed service. Default shutdown action
 * destroys Governator container. A user can provide additional actions to be run
 * either before or after the container shutdown.
 *
 * @author Tomasz Bak
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Deprecated
public class ShutdownModule extends AbstractModule {

    public static final int DEFAULT_PORT = 7002;
    private final int port;

    @Inject
    public ShutdownModule() {
        port = DEFAULT_PORT;
    }

    protected ShutdownModule(int port) {
        this.port = port;
    }

    @Override
    protected void configure() {
        bind(Integer.class).annotatedWith(Names.named("shutdownPort")).toInstance(port);
        bind(ShutdownServer.class).asEagerSingleton();
    }

    protected LinkedBindingBuilder<Action0> bindBeforeShutdownAction() {
        return bind(Action0.class).annotatedWith(Names.named("beforeShutdownAction"));
    }

    protected LinkedBindingBuilder<Action0> bindAfterShutdownAction() {
        return bind(Action0.class).annotatedWith(Names.named("afterShutdownAction"));
    }

    public static BootstrapModule asBootstrapModule() {
        return asBootstrapModule(7002);
    }

    public static BootstrapModule asBootstrapModule(final int port) {
        return Karyon.toBootstrapModule(new ShutdownModule(port));
    }

    @Singleton
    public static class ShutdownServer {
        private static final Logger logger = LoggerFactory.getLogger(ShutdownServer.class);

        @Inject
        @Named("shutdownPort")
        private int port;

        @Inject(optional = true)
        @Named("beforeShutdownAction")
        private Action0 beforeAction;

        @Inject(optional = true)
        @Named("afterShutdownAction")
        private Action0 afterAction;

        @Inject
        private LifecycleManager lifeCycleManager;

        private ShutdownListener shutdownListener;

        @PostConstruct
        public void start() {
            shutdownListener = new ShutdownListener(port, new Action0() {
                @Override
                public void call() {
                    logger.info("Shutdown request received on port {}; stopping all services...", port);
                    try {
                        runShutdownCommands();
                    } catch (Exception e) {
                        logger.error("Errors during shutdown", e);
                    } finally {
                        try {
                            shutdownListener.shutdown();
                        } catch (Exception e) {
                            logger.error("Errors during stopping shutdown listener", e);
                        }
                    }
                }
            });
            shutdownListener.start();
        }

        private void runShutdownCommands() {
            try {
                if (beforeAction != null) {
                    beforeAction.call();
                }
            } finally {
                try {
                    lifeCycleManager.close();
                } finally {
                    if (afterAction != null) {
                        afterAction.call();
                    }
                }
            }
        }
    }
}
