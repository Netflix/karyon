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
package com.netflix.kayron.server.test.client;

import com.netflix.kayron.server.test.RunInKaryon;
import com.netflix.kayron.server.test.configuration.ConfigurationExporter;
import com.netflix.kayron.server.test.configuration.ConfigurationParser;
import com.netflix.kayron.server.test.configuration.Configurations;
import com.netflix.kayron.server.test.configuration.KayronExtensionConfiguration;
import com.netflix.kayron.server.test.server.KayronExtensionConfigurationProducer;
import com.netflix.kayron.server.test.server.KayronRemoteExtension;
import com.netflix.kayron.server.test.server.KayronServerInitializer;
import com.netflix.kayron.server.test.server.KayronTestEnricher;
import com.netflix.kayron.server.test.server.SecurityActions;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
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
public class KayronExtensionArchiveAppender extends CachedAuxilliaryArchiveAppender {

    /**
     * The arquillian descriptor.
     */
    @Inject
    private Instance<ArquillianDescriptor> descriptor;

    /**
     * Builds the archive.
     *
     * @return the archive with all the needed classes.
     */
    @Override
    protected Archive<?> buildArchive() {

        // creates the archive
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "kayron-testsuite.jar");

        // registers all the packages required by the Kayron in order to run in the container
        addRequiredPackages(archive);

        // adds the all needed classes required by the extension
        addExtensionClasses(archive);

        // adds additional properties
        addExtensionProperties(archive);

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

        // adds the implementation classes
        archive.addClass(ConfigurationExporter.class);
        archive.addClass(ConfigurationParser.class);
        archive.addClass(Configurations.class);
        archive.addClass(KayronExtensionConfiguration.class);
        archive.addClass(KayronExtensionConfigurationProducer.class);
        archive.addClass(KayronRemoteExtension.class);
        archive.addClass(KayronRemoteExtension.class);
        archive.addClass(KayronServerInitializer.class);
        archive.addClass(KayronTestEnricher.class);
        archive.addClass(SecurityActions.class);

        // registers the 'in container' extension
        archive.addAsServiceProvider(RemoteLoadableExtension.class, KayronRemoteExtension.class);
    }

    /**
     * Adds the minimal subset of the packages needed to run any Kayron application.
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

        // loads the properties from the arquillian descriptor
        KayronExtensionConfiguration configuration = Configurations.fromDescriptor(descriptor.get()).parse();

        // exports the properties
        String properties = Configurations.toProperties(configuration).exportAsString();

        // adds the properties to the archive
        archive.addAsResource(new StringAsset(properties), "kayron-testsuite.properties");
    }

    /**
     * Retrieves the names of the packages.
     *
     * @return the names of the packages
     */
    private String[] getDependantPackagesNames() {

        return new String[]{
                "com.netflix.governator",
                "com.netflix.kayron",
                "com.google.inject",
        };
    }
}
