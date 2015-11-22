package com.netflix.karyon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.netflix.karyon.annotations.Profiles;
import com.netflix.karyon.conditional.ConditionalSupportModule;
import com.netflix.karyon.conditional.annotations.ConditionalOnProfile;
import com.netflix.karyon.conditional.annotations.ProvidesConditionally;

public class ConditionalProvidesTest {
    public static interface Foo {
        String getName();
    }
    
    public static Foo createFoo(String name) {
        return new Foo() {
            @Override
            public String getName() {
                return name;
            }
        };
    }
    
    public class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            install(new ConditionalSupportModule());
            Multibinder.newSetBinder(binder(), String.class, Profiles.class).addBinding()
                .toInstance("test");
        }
    }
    
    @Test(expected=ConfigurationException.class)
    public void failOnNoBindings() {
        Injector injector = Guice.createInjector(new TestModule());
        
        injector.getInstance(Foo.class);
    }
    
    @Test
    public void succeedOnSingleBinding() {
        Injector injector = Guice.createInjector(new TestModule() {
            @Singleton
            @ProvidesConditionally
            @ConditionalOnProfile("test")
            public Foo getFooTest() {
                return createFoo("test");
            }
        });
        
        Foo foo = injector.getInstance(Foo.class);
        assertThat(foo.getName(), equalTo("test"));
    }
    
    @Test(expected=ProvisionException.class)
    public void failOnNoMetConditionals() {
        Injector injector = Guice.createInjector(new TestModule() {
            @Singleton
            @ProvidesConditionally
            @ConditionalOnProfile("prod")
            public Foo getFooTest() {
                return createFoo("prod");
            }
        });
        
        injector.getInstance(Foo.class);
    }
    
    @Test(expected=ProvisionException.class)
    public void failOnDuplicateMetConditionals() {
        Injector injector = Guice.createInjector(new TestModule() {
            @Singleton
            @ProvidesConditionally
            @ConditionalOnProfile("test")
            public Foo getFooTest1() {
                return createFoo("test1");
            }
            
            @Singleton
            @ProvidesConditionally
            @ConditionalOnProfile("test")
            public Foo getFooTest2() {
                return createFoo("test2");
            }
        });
        
        injector.getInstance(Foo.class);
    }

    @Test
    public void succeedMatchOneConditional() {
        Injector injector = Guice.createInjector(new TestModule() {
            @Singleton
            @ProvidesConditionally
            @ConditionalOnProfile("test")
            public Foo getFooTest1() {
                return createFoo("test");
            }
            
            @Singleton
            @ProvidesConditionally
            @ConditionalOnProfile("prod")
            public Foo getFooTest2() {
                return createFoo("prod");
            }
        });
        
        Foo foo = injector.getInstance(Foo.class);
        assertThat(foo.getName(), equalTo("test"));
    }

    @Test
    public void succeedMatchNoneWithConditional() {
        Injector injector = Guice.createInjector(new TestModule() {
            @Singleton
            @ProvidesConditionally(isDefault=true)
            @ConditionalOnProfile("none1")
            public Foo getFooTest1() {
                return createFoo("none1");
            }
            
            @Singleton
            @ProvidesConditionally
            @ConditionalOnProfile("none2")
            public Foo getFooTest2() {
                return createFoo("none2");
            }
        });
        
        Foo foo = injector.getInstance(Foo.class);
        assertThat(foo.getName(), equalTo("none1"));
    }

    @Test(expected=ProvisionException.class)
    public void failOnMultipleDefaults() {
        Injector injector = Guice.createInjector(new TestModule() {
            @Singleton
            @ProvidesConditionally(isDefault=true)
            @ConditionalOnProfile("none1")
            public Foo getFooTest1() {
                return createFoo("none1");
            }
            
            @Singleton
            @ProvidesConditionally(isDefault=true)
            @ConditionalOnProfile("none2")
            public Foo getFooTest2() {
                return createFoo("none2");
            }
        });
        
        Foo foo = injector.getInstance(Foo.class);
        assertThat(foo.getName(), equalTo("none1"));
    }

    @Test
    public void successOnMultipleQualifiedBindings() {
        Injector injector = Guice.createInjector(new TestModule() {
            @Singleton
            @Named("foo")
            @ProvidesConditionally
            @ConditionalOnProfile("test")
            public Foo getFooTest() {
                return new Foo() {
                    @Override
                    public String getName() {
                        return "getFooTest";
                    }
                    
                    @Override
                    public String toString() {
                        return "Foo[getFooTest]";
                    }
                };
            }
            
            @Singleton
            @Named("foo")
            @ProvidesConditionally
            @ConditionalOnProfile("prod")
            public Foo getFooProd() {
                return new Foo() {
                    @Override
                    public String getName() {
                        return "getFooProd";
                    }
                };
            }
            
            @Singleton
            @Named("bar")
            @ProvidesConditionally
            @ConditionalOnProfile("test")
            public Foo getFooTestBar() {
                return new Foo() {
                    @Override
                    public String getName() {
                        return "getFooTestBar";
                    }

                    @Override
                    public String toString() {
                        return "Foo[getFooTestBar]";
                    }
                };
            }
            
            @Singleton
            @ProvidesConditionally
            @ConditionalOnProfile("test")
            public Foo getUnannotatedFoo() {
                return new Foo() {
                    @Override
                    public String getName() {
                        return "getUnannotatedFoo";
                    }

                    @Override
                    public String toString() {
                        return "Foo[getUnannotatedFoo]";
                    }
                };
            }
        });
        
        Assert.assertEquals("getFooTest", injector.getInstance(Key.get(Foo.class, Names.named("foo"))).getName());
        Assert.assertEquals("getFooTestBar", injector.getInstance(Key.get(Foo.class, Names.named("bar"))).getName());
        Assert.assertEquals("getUnannotatedFoo", injector.getInstance(Key.get(Foo.class)).getName());
    }
}
