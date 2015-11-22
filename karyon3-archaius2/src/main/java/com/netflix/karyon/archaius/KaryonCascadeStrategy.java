package com.netflix.karyon.archaius;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.netflix.karyon.annotations.Profiles;

@Singleton
public class KaryonCascadeStrategy extends InterpolatingCascadeStrategy {

    private final Set<String> profiles;

    @Inject
    public KaryonCascadeStrategy(@Profiles Set<String> profiles) {
        this.profiles = profiles;
    }
    
    @Override
    protected List<String> getPermutations() {
        List<String> permuatations = new ArrayList<>();
        permuatations.add("%s");
        for (String profile : profiles) {
            permuatations.add("%s-" + profile);
        }
        permuatations.addAll(Arrays.asList(
                "%s-${karyon.environment}",
                "${karyon.datacenter}-%s",
                "${karyon.datacenter}-%s-${karyon.environment}"
                ));
        return permuatations;
    }

}
