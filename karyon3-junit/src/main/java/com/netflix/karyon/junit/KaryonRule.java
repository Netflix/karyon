package com.netflix.karyon.junit;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import junit.framework.Assert;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.netflix.governator.DefaultLifecycleListener;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.KaryonModule;

/**
 * JUnit rule to simplify testing with Karyon.  KaryonRule extends Karyon and as such
 * supports all it's methods.  Once start() is called KaryonRule will track the injector
 * that is created and ensure that it is shut down at the end of the test.  Also, any
 * fields of the unit test class passed to KaryonRule are injectable.  
 * 
 * <h3>Usage</h3>
 * 
 * <code>
 public class MyUnitTest {
     {@literal @}Rule
     public KaryonRule karyon = new KaryonRule(this);
     
     {@literal @}Test
     public void someTest() {
         // Configuration the KaryonRule just like you would Karyon
         Injector injector = karyon.addModules(someModules).start();
         
         // Get classes from the injector and assert on conditions
         SomeClassBeingTested obj = injector.getInstance(SomeClassBeingTested.class);
         Assert.assertTrue(obj.someTestCondition());
     }
 }
 
 * </code>
 * 
 * <h3>Injecting into the test class</h3>
 * 
 * <code>
 public class MyUnitTest {
     {@literal @}Rule
     public KaryonRule karyon = new KaryonRule(this);
     
     {@literal @}Inject
     SomeClassBeingTested obj;
     
     {@literal @}Test
     public void someTest() {
         // Configuration the KaryonRule just like you would Karyon
         karyon.addModules(someModules).start();
         
         // Once start is called field's of MyUnitTest will have been injected
         Assert.assertTrue(obj.someTestCondition());
     }
 }
 * </code>
 * 
 * <h3>Test injection</h3>
 * 
 * To test that a type was created and injected (including eager singletons)
 * 
 * <code>
 public class MyUnitTest {
     {@literal @}Rule
     public KaryonRule karyon = new KaryonRule(this);
     
     {@literal @}Test
     public void someTest() {
         // Configuration the KaryonRule just like you would Karyon
         karyon.addModules(someModules).start();
         
         // This assertion will pass iff SomeType was instantiated
         Assert.assertTrue(karyon.didInject(SomeType.class));
     }
 }
 * </code>
 * 
 * @author elandau
 *
 */
public class KaryonRule extends Karyon implements TestRule {
    
    private LifecycleInjector injector;
    private Object obj;
    private Set<TypeLiteral<?>> injectedTypes = new CopyOnWriteArraySet<>();
    static class TrackingLifecycleListener extends DefaultLifecycleListener {
        private boolean stopped = false;
        
        @Override
        public void onStopped() {
            this.stopped = true;
        }

        @Override
        public void onStartFailed(Throwable t) {
            this.stopped = true;
        }
    }
    
    public KaryonRule(Object obj) {
        this(obj, null);
    }

    public KaryonRule(Object obj, KaryonModule module) {
        this.obj = obj;
        this.injector = null;
        
        if (module != null) 
            this.apply(module);
    }

    public LifecycleInjector getInjector() {
        return start();
    }
    
    /**
     * start() the Injector and cache internally so it can be shutdown
     * at the end of the test
     */
    public LifecycleInjector start() {
        if (this.injector == null) {
            addModules(new AbstractModule() {
                @Override
                protected void configure() {
                    this.bindListener(Matchers.any(), new TypeListener() {
                        @Override
                        public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> encounter) {
                            encounter.register(new InjectionListener<I>() {
                                @Override
                                public void afterInjection(I injectee) {
                                    injectedTypes.add(typeLiteral);
                                }
                            });
                        }
                    });
                }
            });
            injector = super.start();
            injector.injectMembers(obj);
        }
        else {
            throw new RuntimeException("Already started");
        }
        return injector;
    }
    
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } finally {
                    if (injector != null) {
                        TrackingLifecycleListener listener = new TrackingLifecycleListener();
                        injector.addListener(listener);
                        injector.shutdown();
                        injector.awaitTermination();
                        
                        Assert.assertTrue(listener.stopped);
                    }
                }
            }
        };
    }
    
    public boolean didInject(Class<?> type) {
        return didInject(TypeLiteral.get(type));
    }

    public boolean didInject(TypeLiteral<?> type) {
        return injectedTypes.contains(type);
    }

}
