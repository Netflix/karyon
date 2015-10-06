package com.netflix.karyon;

public class DefaultKaryonSuite extends AbstractKaryonDslModule {
    @Override
    protected void configure() throws Exception {
        // TODO: This should probably not be added for ALL instances of KaryonBuilder.
        addAutoModuleListProvider(ModuleListProviders.forPackagesConditional("com.netflix.karyon"));
        addAutoModuleListProvider(ModuleListProviders.forPackagesConditional("com.google.inject.servlet"));
    }
}
