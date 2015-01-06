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

package netflix.adminresources.resources;

import com.netflix.config.ConfigurationManager;
import netflix.adminresources.AdminResourcesContainer;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * @author Nitesh Kant
 */
public class WebAdminComponent {

    private static final Logger logger = LoggerFactory.getLogger(WebAdminComponent.class);
    public static final String ADMINRES_WEBADMIN_INDEX_HTML = "/baseserver";

    @PostConstruct
    public void init() {
        final AbstractConfiguration configInst = ConfigurationManager.getConfigInstance();

        if (configInst.containsKey(AdminResourcesContainer.DEFAULT_PAGE_PROP_NAME)) {
            logger.info("Admin container default page already set to: " +
                    configInst.getString(AdminResourcesContainer.DEFAULT_PAGE_PROP_NAME + ", not overriding."));
            return;
        }
        configInst.setProperty(AdminResourcesContainer.DEFAULT_PAGE_PROP_NAME, ADMINRES_WEBADMIN_INDEX_HTML);
        logger.info("Set the default page for admin container to: " + ADMINRES_WEBADMIN_INDEX_HTML);
    }
}
