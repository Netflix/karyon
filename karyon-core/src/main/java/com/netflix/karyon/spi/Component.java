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

package com.netflix.karyon.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations to indicate that a class is a component. <br/>
 * All components will be created by karyon using governator at startup before initializing {@link Application}. <p/>
 *
 * Karyon initializes all components discovered by governator's classpath scanning at startup. See
 * {@link com.netflix.karyon.spi.Component#disableProperty()} for details on how to disable a component from being
 * initialized. <br/>
 * If you want to hand-pick the components to be initialized, set a property {@link PropertyNames#EXPLICIT_COMPONENT_CLASSES_PROP_NAME}
 * with the comma separated classnames of the components.<br/>
 *
 * @author Nitesh Kant
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

    /**
     * Name of the property accessible to <a href="https://github.com/Netflix/archaius/">Archaius</a> which if set to
     * <code>true</code>, this component will not be initialized. All components are initialized by default and have
     * to be "opted-out" on purpose. <br/>
     * If this property is not set, karyon will also look at a property
     * {@link PropertyNames#COMPONENT_DISABLE_PROP_PEFIX}.[classname of the component], which if set to true, will
     * disable the component. eg: If your component is defined as:
     *
     <PRE>
     package foo.bar;

     {@literal @}Component
     public class MyComponent {
     }
     </PRE>

     The above component can be disabled setting a property "com.netflix.karyon.component.disable.foo.bar.MyComponent"
     to <code>true</code>. On the other hand, if the component was defined as:

     <PRE>
     package foo.bar;

     {@literal @}Component(disableProperty="mycomp.disable")
     public class MyComponent {
     }
     </PRE>
     * The above component can be disabled setting a property "mycomp.disable"
     * to <code>true</code>.
     *
     * @return Name of the property which if set to true will disable this component.
     */
    String disableProperty() default "";
}
