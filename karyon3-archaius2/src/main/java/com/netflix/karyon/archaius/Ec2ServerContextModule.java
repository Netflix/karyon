package com.netflix.karyon.archaius;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.netflix.archaius.guice.ConfigSeeder;
import com.netflix.archaius.inject.DefaultsLayer;
import com.netflix.karyon.conditional.ConditionalOnEc2;

@ConditionalOnEc2
public class Ec2ServerContextModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), ConfigSeeder.class, DefaultsLayer.class).addBinding().to(Ec2ServerContextConfigSeeder.class);
    }

}
