package com.netflix.karyon;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import com.google.inject.Stage;

public class DefaultKaryonConfigurationTest {
    @Test
    public void testConfiguration() {
        DefaultKaryonConfiguration config = new DefaultKaryonConfiguration();
        assertThat(config.getBootstrapModules().size(), equalTo(0));
        assertThat(config.getProfiles().size(), equalTo(0));
        assertThat(config.getStage(), equalTo(Stage.DEVELOPMENT));
        assertThat(config.getModuleListProviders().size(), equalTo(0));
    }
}
