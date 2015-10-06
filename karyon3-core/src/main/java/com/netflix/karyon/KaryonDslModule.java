package com.netflix.karyon;

/**
 * Sequence of calls to Karyon captured in a single class that can be applied in one
 * call to the KaryonDsl.
 * 
 * <code>
 
 Karyon.newServer()
     .using(new MyKaryonDslModule())
     .start();
 
 * </code>
 * 
 * @author elandau
 *
 */
public interface KaryonDslModule {
    void configure(KaryonDsl<?> builder) throws Exception;
}
