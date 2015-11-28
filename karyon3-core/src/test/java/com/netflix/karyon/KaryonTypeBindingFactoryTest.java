package com.netflix.karyon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.netflix.karyon.annotations.Priority;
import com.netflix.karyon.spi.AbstractAutoBinder;

public class KaryonTypeBindingFactoryTest {
    public static interface Baz {
        String getName();
    }
    
    public static interface Bar {
        String getName();
    }
    
    public static class Foo {
        private Bar bar;
        private Baz baz;
        private Bar namedBar;

        @Inject
        public Foo(Bar bar, Baz baz, @Named("bar") Bar namedBar) {
            this.bar = bar;
            this.baz = baz;
            this.namedBar = namedBar;
        }
    }
    
    public static class Foo2 {
        private Bar bar;

        @Inject
        public Foo2(Bar bar) {
            this.bar = bar;
        }
    }
    
    @Test(expected=CreationException.class)
    public void confirmCreateFailsWithMissingBinding() {
        Karyon.newBuilder()
            .addModules(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Foo.class).asEagerSingleton();
                }
            })
            .start();
    }
    
    @Test
    public void testAddBinding() {
        Injector injector = Karyon.newBuilder()
            .addModules(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Foo.class).asEagerSingleton();
                }
            })
            .addAutoBinder(new BarAutoBinder())
            .addAutoBinder(new BazAutoBinder())
            .start();
            
        Foo foo = injector.getInstance(Foo.class);
        
        assertThat(foo.bar.getName(),       equalTo(""));
        assertThat(foo.baz.getName(),       equalTo(""));
        assertThat(foo.namedBar.getName(),  equalTo("@com.google.inject.name.Named(value=bar)"));
    }
    
    @Test
    public void testPriority() {
        Injector injector = Karyon.newBuilder()
            .addModules(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Foo2.class).asEagerSingleton();
                }
            })
            .addAutoBinder(new BarAutoBinder())
            .addAutoBinder(new BarAutoBinder2())
            .start();
            
        Foo2 foo = injector.getInstance(Foo2.class);
        
        System.out.println("Found: " + foo.bar.getName());
        assertThat(foo.bar.getName(),       equalTo("-1"));
    }
    
    @Priority(1)
    static class BarAutoBinder extends AbstractAutoBinder {
        public BarAutoBinder() {
            super(KeyMatchers.subclassOf(Bar.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Module getModuleForKey(final Key<T> key) {
            return new AbstractModule() {
                @Override
                protected void configure() {
                    bind(key).toInstance((T)new Bar() {
                        @Override
                        public String getName() {
                            return null == key.getAnnotation() ? "" : key.getAnnotation().toString();
                        } 
                    });
                }
            };
        }
    }
    
    @Priority(2)
    static class BarAutoBinder2 extends AbstractAutoBinder {
        public BarAutoBinder2() {
            super(KeyMatchers.subclassOf(Bar.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Module getModuleForKey(final Key<T> key) {
            return new AbstractModule() {
                @Override
                protected void configure() {
                    bind(key).toInstance((T)new Bar() {
                        @Override
                        public String getName() {
                            return null == key.getAnnotation() ? "-1" : key.getAnnotation().toString() + "-1";
                        } 
                    });
                }
            };
        }
    }
    
    static class BazAutoBinder extends AbstractAutoBinder {
        public BazAutoBinder() {
            super(KeyMatchers.subclassOf(Baz.class));
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <T> Module getModuleForKey(Key<T> key) {
            return new AbstractModule() {
                @Override
                protected void configure() {
                    bind(key).toInstance((T)new Baz() {
                        @Override
                        public String getName() {
                            return null == key.getAnnotation() ? "" : key.getAnnotation().toString();
                        }
                    });
                }
            };
        }
    }
}   
