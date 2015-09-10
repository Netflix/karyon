package com.netflix.karyon.archaius;

import com.google.inject.multibindings.Multibinder;
import com.netflix.archaius.guice.ConfigSeeder;
import com.netflix.archaius.inject.DefaultsLayer;
import com.netflix.governator.DefaultModule;
import com.netflix.karyon.conditional.ConditionalOnLocalDev;

@ConditionalOnLocalDev
public class LocalServerContextModule extends DefaultModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ConfigSeeder.class, DefaultsLayer.class).addBinding().to(LocalServerContextConfigSeeder.class);
    }

}
