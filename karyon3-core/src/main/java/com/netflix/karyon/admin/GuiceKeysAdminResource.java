package com.netflix.karyon.admin;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.netflix.governator.ProvisionMetrics;

@Singleton
public class GuiceKeysAdminResource {
    private final ProvisionMetrics metrics;
    private final Injector injector;

    @Inject
    public GuiceKeysAdminResource(ProvisionMetrics metrics, Injector injector) {
        this.metrics = metrics;
        this.injector = injector;
    }
    
    // /guice-keys/
    public Set<String> get() {
        Set<String> modules = new HashSet<>();
        for (Entry<Key<?>, Binding<?>> binding : injector.getAllBindings().entrySet()) {
            modules.add(binding.getKey().toString());
        }
        return modules;
    }
}
