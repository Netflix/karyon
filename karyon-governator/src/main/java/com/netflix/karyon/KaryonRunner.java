package com.netflix.karyon;

import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.governator.guice.annotations.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An application runner which consumes a main class annotated with governator's {@link Bootstrap} annotations and
 * executes that main class using {@link LifecycleInjector#bootstrap(Class, LifecycleInjectorBuilderSuite...)}.
 * For applications using a programmatic module configuration instead of annotation based module discovery, it is
 * better to instead use {@link Karyon}
 *
 * @author Nitesh Kant
 */
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
            Karyon.forApplication(Class.forName(mainClassName), (LifecycleInjectorBuilderSuite[]) null)
                  .startAndWaitTillShutdown();
        } catch (@SuppressWarnings("UnusedCatchParameter") ClassNotFoundException e) {
            System.out.println("Main class: " + mainClassName + "not found.");
            System.exit(-1);
        } catch (Exception e) {
            logger.error("Error while starting karyon server.", e);
            System.exit(-1);
        }

        // In case we have non-daemon threads running
        System.exit(0);
    }
}
