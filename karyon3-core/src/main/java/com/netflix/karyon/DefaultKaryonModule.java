package com.netflix.karyon;

public class DefaultKaryonModule extends AbstractKaryonModule {
    @Override
    protected void configure() {
        // TODO: This should probably not be added for ALL instances of KaryonBuilder.
        addAutoModuleListProvider(ModuleListProviders.forPackagesConditional("com.netflix.karyon"));
        addAutoModuleListProvider(ModuleListProviders.forPackagesConditional("com.google.inject.servlet"));
    }
}
