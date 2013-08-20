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
package com.netflix.hellonoss.utils;

import com.google.inject.servlet.GuiceFilter;
import com.netflix.hellonoss.core.HelloworldComponent;
import com.netflix.hellonoss.server.HelloWorldBootstrap;
import com.netflix.hellonoss.server.health.HealthCheck;
import com.netflix.kayron.server.test.KayronTestGuiceContextListener;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

/**
 * A utility class that is responsible for creating the depolyment.
 *
 * @author Jakub Narloch (jmnarloch@gmail.com)
 */
public final class Deployments {

    /**
     * Creates new instance of {@link Deployments} class.
     * <p />
     * The private constructor prevents from instantiation outside of this class.
     */
    private Deployments() {
        // empty constructor
    }

    /**
     * Creates the test deployment containing the whole component.
     *
     * @return the archive with the micro deployment of the component
     */
    public static Archive createDeployment() {

        // adds all the dependencies needed to run the tests
        // the jersey is used by the test application since it's exposing the JAX-RS endpoint
        File[] libs = Maven.resolver()
                .resolve("com.sun.jersey.contribs:jersey-guice:1.8",
                        "org.codehaus.jackson:jackson-mapper-asl:1.9.12")
                .withoutTransitivity()
                .asFile();

        // creates the deployment
        return ShrinkWrap.create(WebArchive.class, "hello-netflix-oss.war")
                .addPackage(HelloworldComponent.class.getPackage())
                .addPackage(HelloWorldBootstrap.class.getPackage())
                .addPackage(HealthCheck.class.getPackage())
                .addAsResource("eureka-client.properties")
                .addAsResource("hello-netflix-oss.properties")
                .addAsResource("hello-netflix-oss-dev.properties")
                .addAsResource("simplelogger.properties")
                .setWebXML(new StringAsset(createDescriptor()))
                .addAsLibraries(libs);
    }

    /**
     * Creates the web descriptor for the deployment.
     *
     * @return the web descriptor
     */
    private static String createDescriptor() {

        return Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .createFilter()
                .filterName("guiceFilter")
                .filterClass(GuiceFilter.class.getName()).up()
                .createFilterMapping()
                .filterName("guiceFilter")
                .urlPattern("/*").up()
                .createListener()
                .listenerClass(KayronTestGuiceContextListener.class.getName()).up()
                .exportAsString();
    }
}
