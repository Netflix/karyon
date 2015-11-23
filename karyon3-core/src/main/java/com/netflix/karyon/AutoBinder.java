package com.netflix.karyon;

import com.google.inject.Binder;
import com.google.inject.Key;

/**
 * Automatically create bindings when none exist.  This is an extension to Guice which does not 
 * provide a catch-all provider for missing bindings.  
 * 
 * For example, the following code will fail with a missing binding error for Foo
 * 
 * <code>
  public interface Foo {
  }
  
  public class Bar {
      {@literal @}Inject
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
 * </code>
 * 
 * A catch-all binder for Foo can can added as follows
 * 
 * <code>
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
                    {@literal @}Override
                    public {@literal <}T{@literal >} boolean bind(Binder binder, final Key{@literal <}T{@literal >} key) {
                        binder.bind(key).toInstance((T)new Foo() {});
                        return true;
                    }
                }
            )
 *          .start();
 * }
 * </code>
 * 
 * This functionality is useful to reduce boiler plate code.
 * 
 */
public interface AutoBinder {
    /**
     * Create a bindings for the specified type literal.  
     * 
     * @param binder
     * @param key  
     * 
     * @return True if bindings was created or false if not.
     */
    <T> boolean configure(Binder binder, Key<T> key);
}
