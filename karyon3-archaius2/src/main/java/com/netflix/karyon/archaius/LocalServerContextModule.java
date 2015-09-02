package com.netflix.karyon.archaius;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.netflix.archaius.guice.ConfigSeeder;
import com.netflix.archaius.inject.DefaultsLayer;
import com.netflix.governator.auto.annotations.Bootstrap;
import com.netflix.karyon.conditional.ConditionalOnLocalDevTest;

@ConditionalOnLocalDevTest
@Bootstrap
public class LocalServerContextModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ConfigSeeder.class, DefaultsLayer.class).addBinding().to(LocalServerContextConfigSeeder.class);
    }

}
