/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package com.netflix.karyon.server.test.client;

import com.netflix.karyon.server.test.KaryonTestGuiceContextListener;
import com.netflix.karyon.server.test.RunInKaryon;
import com.netflix.karyon.server.test.configuration.ConfigurationExporter;
import com.netflix.karyon.server.test.configuration.ConfigurationParser;
import com.netflix.karyon.server.test.configuration.Configurations;
import com.netflix.karyon.server.test.configuration.KaryonExtensionConfiguration;
import com.netflix.karyon.server.test.server.KaryonExtensionRemoteConfigurationProducer;
import com.netflix.karyon.server.test.server.KaryonRemoteExtension;
import com.netflix.karyon.server.test.server.KaryonServerInitializer;
import com.netflix.karyon.server.test.server.KaryonTestEnricher;
import com.netflix.karyon.server.test.server.SecurityActions;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * It's responsible for packaging all the classes needed by this extension in order to run.
 * <p />
 * Basically it adds all the API classes and also the implementation needed to run on the server side when it will be
 * invoked by the Arquillian runtime.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KaryonExtensionArchiveAppender extends CachedAuxilliaryArchiveAppender {

    /**
     * The arquillian descriptor.
     */
    @Inject
    private Instance<KaryonExtensionConfiguration> configuration;

    /**
     * Builds the archive.
     *
     * @return the archive with all the needed classes.
     */
    @Override
    protected Archive<?> buildArchive() {

        // creates the archive
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "karyon-testsuite.jar");

        // adds the all needed classes required by the extension
        addExtensionClasses(archive);

        // adds additional properties
        addExtensionProperties(archive);

        if (configuration.get().isAutoPackage()) {

            // registers all the packages required by the Karyon in order to run in the container
            addRequiredPackages(archive);
        }

        // returns the created archive
        return archive;
    }

    /**
     * Adds all the classes needed by the this extension.
     *
     * @param archive the archive
     */
    private void addExtensionClasses(JavaArchive archive) {

        // adds the 'API' classes
        archive.addClass(RunInKaryon.class);
        archive.addClass(KaryonTestGuiceContextListener.class);

        // adds the implementation classes
        archive.addClass(ConfigurationExporter.class);
        archive.addClass(ConfigurationParser.class);
        archive.addClass(Configurations.class);
        archive.addClass(KaryonExtensionConfiguration.class);
        archive.addClass(KaryonExtensionRemoteConfigurationProducer.class);
        archive.addClass(KaryonRemoteExtension.class);
        archive.addClass(KaryonRemoteExtension.class);
        archive.addClass(KaryonServerInitializer.class);
        archive.addClass(KaryonTestEnricher.class);
        archive.addClass(SecurityActions.class);

        // registers the 'in container' extension
        archive.addAsServiceProvider(RemoteLoadableExtension.class, KaryonRemoteExtension.class);
    }

    /**
     * Adds the minimal subset of the packages needed to run any Karyon application.
     *
     * @param archive the archive
     */
    private void addRequiredPackages(JavaArchive archive) {

        archive.addPackages(true, getDependantPackagesNames());
    }

    /**
     * Adds the the properties required by the extension.
     *
     * @param archive the archive
     */
    private void addExtensionProperties(JavaArchive archive) {

        // exports the properties
        String properties = Configurations.toProperties(configuration.get()).exportAsString();

        // adds the properties to the archive
        archive.addAsResource(new StringAsset(properties), "karyon-testsuite.properties");
    }

    /**
     * Retrieves the names of the packages.
     *
     * @return the names of the packages
     */
    private String[] getDependantPackagesNames() {

        return new String[]{
                "com.netflix.governator",
                "com.netflix.karyon",
                "com.google.inject",
        };
    }
}
