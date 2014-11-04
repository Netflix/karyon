package netflix.karyon.servo;

import com.google.inject.AbstractModule;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.servo.ServoEventsListenerFactory;

/**
 * @author Nitesh Kant
 */
public class KaryonServoModule extends AbstractModule{

    @Override
    protected void configure() {
        RxNetty.useMetricListenersFactory(new ServoEventsListenerFactory());
    }

    public static LifecycleInjectorBuilderSuite asSuite() {
        return new LifecycleInjectorBuilderSuite() {
            @Override
            public void configure(LifecycleInjectorBuilder builder) {
                builder.withAdditionalModules(new KaryonServoModule());
            }
        };
    }
}
