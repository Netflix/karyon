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
import com.netflix.kayron.server.test.server.KayronRemoteExtension;
import com.netflix.kayron.server.test.server.KayronServerInitializer;
import com.netflix.kayron.server.test.server.KayronTestEnricher;
import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
        archive.addClass(KayronRemoteExtension.class);
        archive.addClass(KayronServerInitializer.class);
        archive.addClass(KayronTestEnricher.class);

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
