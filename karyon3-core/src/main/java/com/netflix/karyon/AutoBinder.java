package com.netflix.karyon;

import com.google.inject.Binder;
import com.google.inject.Key;

/**
 * Automatically create bindings when none exist.  This is an extension to Guice which does not 
 * provide a catch-all provider for missing bindings.  
 * 
 * For example, the following code will fail with a missing binding error for Foo
 * 
 * ```java
  public interface Foo {
  }
  
  public class Bar {
      @Inject
      public Bar(Foo foo) {
      }
  }
   
  public static void main() {
      Karyon.create()
            .addModule(new AbstractModule() {
               protected void configure() {
                   bind(Bar.class).asEagerSingleton();
               }
            }
            .start();
   }
 * ```
 * 
 * A catch-all binder for Foo can can added as follows
 * 
 * ```java
   public static void main() {
       Karyon.create()
            .addModule(new AbstractModule() {
                protected void configure() {
                    bind(Bar.class).asEagerSingleton();
                }
            }
            .addTypeBindingFactory(
                TypeLiteralMatchers.subclassOf(Bar.class),
                new KeyAutoBinder() {
                    @Override
                    public <T> boolean bind(Binder binder, final Key<T> key) {
                        binder.bind(key).toInstance((T)new Foo() {});
                        return true;
                    }
                }
            )
 *          .start();
 * }
 * ```
 * 
 * This functionality is useful to reduce boiler plate code.
 * 
 */
public interface AutoBinder {
    /**
     * Create a bindings for the specified type literal.  
     * 
     * @param key  
     * @param binder
     * 
     * @return True if bindings was created or false if not.
     */
    <T> boolean configure(Binder binder, Key<T> key);
}
