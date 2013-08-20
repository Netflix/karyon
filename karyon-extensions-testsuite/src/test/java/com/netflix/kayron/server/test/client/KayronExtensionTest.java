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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link KayronExtension} class.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class KayronExtensionTest {

    /**
     * Represents the instance of the tested class.
     */
    private KayronExtension instance;

    /**
     * Represents the instance of the extension builder.
     */
    @Mock
    private LoadableExtension.ExtensionBuilder extensionBuilder;

    /**
     * Sets up the model environment.
     */
    @Before
    public void setUp() {

        // given
        instance = new KayronExtension();

        when(extensionBuilder.service(any(Class.class), any(Class.class))).thenReturn(extensionBuilder);
    }

    /**
     * Tests the {@link KayronExtension#register(LoadableExtension.ExtensionBuilder)} method.
     */
    @Test
    public void shouldRegisterExtension() {

        // when
        instance.register(extensionBuilder);

        // then
        verify(extensionBuilder).service(AuxiliaryArchiveAppender.class, KayronExtensionArchiveAppender.class);
        verifyNoMoreInteractions(extensionBuilder);
    }
}
