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

package netflix.adminresources;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import netflix.admin.RedirectRules;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Singleton
public class RedirectFilter implements Filter {

    private RedirectRules redirectRules;

    @Inject
    public RedirectFilter(RedirectRules redirectRules) {
        this.redirectRules = redirectRules;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        final String requestURI = httpRequest.getRequestURI();

        // redirect based on a simple table lookup
        final Map<String, String> mappings = redirectRules.getMappings();
        for (Map.Entry<String, String> mapping : mappings.entrySet()) {
            if (requestURI.equals(mapping.getKey())) {
                ((HttpServletResponse) response).sendRedirect(mapping.getValue());
                return;
            }
        }

        // redirect based on a custom logic for request
        final String redirectTo = redirectRules.getRedirect(httpRequest);
        if (redirectTo != null && !redirectTo.isEmpty() && !redirectTo.equals(requestURI)) {
            ((HttpServletResponse) response).sendRedirect(redirectTo);
            return;
        }

        chain.doFilter(httpRequest, response);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
    }
}
