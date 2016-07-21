package netflix.karyon;

import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.annotations.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An application runner which consumes a main class annotated with governator's {@link Bootstrap} annotations.
 *
 * This is shorthand for:
 *
 <PRE>
     com.netflix.karyon.forApplication(MyApp.class).startAndWaitTillShutdown()
 </PRE>
 *
 * where the name of the Application class is passed as the argument to the main method.
 *
 * This is useful while creating standard packaging scripts where the main class for starting the JVM remains the same
 * and the actual application class differs from one application to another.
 *
 * If you are bootstrapping karyon programmatically, it is better to use {@code Karyon} directly.
 *
 * @author Nitesh Kant
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Deprecated
public class KaryonRunner {

    private static final Logger logger = LoggerFactory.getLogger(KaryonRunner.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: " + KaryonRunner.class.getCanonicalName() + " <main classs name>");
            System.exit(-1);
        }

        String mainClassName = args[0];
        System.out.println("Using main class: " + mainClassName);

        try {
            Karyon.forApplication(Class.forName(mainClassName), (BootstrapModule[]) null)
                  .startAndWaitTillShutdown();
        } catch (@SuppressWarnings("UnusedCatchParameter") ClassNotFoundException e) {
            System.out.println("Main class: " + mainClassName + " not found.");
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Error while starting karyon server.", e);
            System.exit(-1);
        }

        // In case we have non-daemon threads running
        System.exit(0);
    }
}
