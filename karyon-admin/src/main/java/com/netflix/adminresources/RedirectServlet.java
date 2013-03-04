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

package com.netflix.adminresources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A {@link javax.servlet.Servlet} to handle redirecting old links (currently from NAC) of the
 * form http://<hostname>:8077/AdminNetflixConfiguration/... - this servlet will
 * simply redirect to a new explorers based URL for the same functionality OR
 * emit the expected response itself. It will also handle redirecting "/" to
 * default view (baseserver unless overridden)
 * 
 * @author pkamath
 * @author Nitesh Kant
 */
@SuppressWarnings("serial")
public class RedirectServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RedirectServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Redirect servlet invoked, redirecting to healthcheck.");
        response.sendRedirect("/healthcheck");
    }
}
