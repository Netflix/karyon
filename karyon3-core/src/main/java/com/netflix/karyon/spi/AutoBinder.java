package com.netflix.karyon.spi;

import com.google.inject.Key;
import com.google.inject.Module;

/**
 * Automatically create bindings when none exist.  This is an extension to Guice as it
 * does not provide a catch-all provider for missing bindings.  
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
            .addAutoBinder(
                new AbstractAutoBinder(TypeLiteralMatchers.subclassOf(Foo.class)) {
                    {@literal @}Override
                    public {@literal <}T{@literal >} boolean bind(Binder binder, final Key{@literal <}T{@literal >} key) {
                        binder.bind(key).toInstance((T)new Foo() {});
                        return true;
                    }
                }
            )
            .start();
  }
 * </code>
 * 
 * Note that for auto-binding to work all injection points must be known prior to creating
 * the injector.  All classes that are directly used must have a defined binding, such as Bar above.
 * 
 * This feature is useful where an application or library developer opts to completely decouple their
 * code of any knowledge of how a type is constructed instead deferring this responsibility to the framework
 * in which the application is running. 
 */
public interface AutoBinder {
    /**
     * Create a binding for the specified type literal.  This may include installing modules and specifying
     * additional binds that may be needed.  
     * 
     * @param key Key for which no binding was found
     * 
     * @return Module for creating the bindings
     */
    <T> Module getModuleForKey(Key<T> key);
    
    /**
     * @param t The type
     * @return Returns true if the AutoBinder can provide binding for this type
     */
    boolean matches(Key<?> t);

}
