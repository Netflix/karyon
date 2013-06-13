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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Pattern;
import com.google.inject.Singleton;

/**
 * A {@link Filter} implementation to capture request details like remote host, HTTP method and request URI
 * 
 * @author pkamath
 * @author Nitesh Kant
 */
@Singleton
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    // We will not log requests for these file extensions
    private static Pattern PATTERN_FOR_CSS_JS_ETC = Pattern.compile(".*js$|.*png$|.*gif$|.*css$|.*jpg$|.*jpeg$|.*ico$");

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (!PATTERN_FOR_CSS_JS_ETC.matcher(httpRequest.getRequestURI())
                .matches()) {
            StringBuilder sb = new StringBuilder("AdminResources request details: ");
            sb.append(httpRequest.getRemoteHost()).append(" ")
                    .append(httpRequest.getRemoteAddr()).append(" ")
                    .append(httpRequest.getMethod()).append(" ")
                    .append(httpRequest.getRequestURI());
            logger.info(sb.toString());
        }
        chain.doFilter(httpRequest, response);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

}
