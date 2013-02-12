/*
 * Copyright 2013 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.netflix.karyon.lifecycle;

import com.google.inject.Binder;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.annotations.AutoBind;
import com.netflix.governator.guice.AutoBindProvider;
import com.netflix.governator.lifecycle.ClasspathScanner;
import com.netflix.karyon.spi.Application;
import com.netflix.karyon.spi.Component;
import org.apache.commons.configuration.AbstractConfiguration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class KaryonAutoBindProvider implements AutoBindProvider<AutoBind>{

    public static final String APPLICATION_SCANNER_NAME = "karyon.internal.classpath.scanner.application";
    public static final String COMPONENT_SCANNER_NAME = "karyon.internal.classpath.scanner.component";
    public static final String ARCHIAUS_CONFIG_NAME = "karyon.internal.archaius.config.instance";

    private ClasspathScanner appScanner;
    private ClasspathScanner componentScanner;


    public KaryonAutoBindProvider(Collection<String> scanPackages) {
        if (null != scanPackages) {
            Collection<Class<? extends Annotation>> annotationClasses = new ArrayList<Class<? extends Annotation>>();
            annotationClasses.add(Application.class);
            appScanner = new ClasspathScanner(scanPackages, annotationClasses);

            annotationClasses = new ArrayList<Class<? extends Annotation>>();
            annotationClasses.add(Component.class);
            componentScanner = new ClasspathScanner(scanPackages, annotationClasses);
        }
    }

    @Override
    public void configure(Binder binder, AutoBind autoBindAnnotation) {
        String value = autoBindAnnotation.value();
        if (null == value) {
            return;
        }
        if (value.equals(APPLICATION_SCANNER_NAME)) {
            binder.bind(ClasspathScanner.class).annotatedWith(autoBindAnnotation).toInstance(appScanner);
        } else if (value.equals(COMPONENT_SCANNER_NAME)) {
            binder.bind(ClasspathScanner.class).annotatedWith(autoBindAnnotation).toInstance(componentScanner);
        } else if (value.equals(ARCHIAUS_CONFIG_NAME)) {
            binder.bind(AbstractConfiguration.class).annotatedWith(autoBindAnnotation).toInstance(ConfigurationManager.getConfigInstance());
        }
    }
}
