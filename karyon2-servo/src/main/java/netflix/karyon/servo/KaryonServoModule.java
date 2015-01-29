package netflix.karyon.servo;

import com.google.inject.AbstractModule;
import com.netflix.governator.guice.BootstrapModule;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.servo.ServoEventsListenerFactory;
import netflix.karyon.Karyon;

/**
 * Register global RxNetty's Metric Listeners Factory to use Servo.
 *
 * @author Nitesh Kant
 */
public class KaryonServoModule extends AbstractModule {

    @Override
    protected void configure() {
        RxNetty.useMetricListenersFactory(new ServoEventsListenerFactory());
    }

    public static BootstrapModule asBootstrapModule() {
        return Karyon.toBootstrapModule(KaryonServoModule.class);
    }
}
