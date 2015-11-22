package com.netflix.karyon.conditional;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.karyon.annotations.Profiles;
import com.netflix.karyon.conditional.annotations.ConditionalOnProfile;

@Singleton
class ConditionalOnProfileMatcher implements ConditionalMatcher<ConditionalOnProfile>{
    @Inject
    @Profiles
    Set<String> profiles;
    
    @Override
    public boolean evaluate(ConditionalOnProfile conditional) {
        return profiles.contains(conditional.value());
    }
}
