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

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * An entry point of the extension that boostraps all the needed classes.
 * <p />
 * The extension is being registered through LoadableExtension file located in the META-INF/services directory.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public class KayronExtension implements LoadableExtension {

    /**
     * Registers the extension.
     *
     * @param builder the extension builder
     */
    @Override
    public void register(ExtensionBuilder builder) {

        // registers the extension archive appender
        builder.service(AuxiliaryArchiveAppender.class, KayronExtensionArchiveAppender.class);
    }
}
