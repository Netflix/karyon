/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package netflix.admin;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.netflix.explorers.ExplorerManager;
import com.netflix.explorers.context.GlobalModelContext;
import com.netflix.explorers.context.RequestContext;
import com.netflix.explorers.providers.ToJsonMethod;
import com.sun.jersey.api.view.Viewable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;

public class AdminFreemarkerTemplateProvider implements MessageBodyWriter<Viewable> {
    private static final Logger LOG = LoggerFactory.getLogger(AdminFreemarkerTemplateProvider.class);
    private static final String ADMIN_CONSOLE_LAYOUT = "bootstrap";

    private Configuration fmConfig = new Configuration();
    private ExplorerManager manager;

    @Context
    private ThreadLocal<HttpServletRequest> requestInvoker;
    
    class ViewableResourceTemplateLoader extends URLTemplateLoader {
		static final String KEY_NETFLIX_ADMIN_REQUEST_VIEWABLE = "netflix.admin.request.viewable";

		@Override
		protected URL getURL(String name) {
			URL viewResource = null;
			Viewable viewable = (Viewable)requestInvoker.get().getAttribute(ViewableResourceTemplateLoader.KEY_NETFLIX_ADMIN_REQUEST_VIEWABLE);
			if (viewable != null && viewable.getResolvingClass() != null) {
				viewResource = viewable.getResolvingClass().getResource(name);				
			}
			return viewResource;
		}
    	
    }

    @Inject
    public AdminFreemarkerTemplateProvider(AdminExplorerManager adminExplorerManager) {
        manager = adminExplorerManager;
    }

    @PostConstruct
    public void commonConstruct() {
        // Just look for files in the class path
        fmConfig.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{
        		new ViewableResourceTemplateLoader(),
        		new ClassTemplateLoader(getClass(), "/"),
        		}));
        fmConfig.setNumberFormat("0");
        fmConfig.setLocalizedLookup(false);
        fmConfig.setTemplateUpdateDelay(0);

        try {
            if (manager != null) {
                fmConfig.setSharedVariable("Global", manager.getGlobalModel());
                fmConfig.setSharedVariable("Explorers", manager);
            }
            fmConfig.setSharedVariable("toJson", new ToJsonMethod());
        } catch (TemplateModelException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public long getSize(Viewable t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return !(!(mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)
                || mediaType.isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE))
                || !Viewable.class.isAssignableFrom(type));
    }

    /**
     * Write the HTML by invoking the FTL template
     * <p/>
     * Variables accessibile to the template
     * <p/>
     * it         - The 'model' provided by the controller
     * Explorer   - IExplorerModule reference
     * Explorers  - Map of all explorer modules
     * Global     - Global variables from the ExploreModule manager
     * Request    - The HTTPRequestHandler
     * Instance   - Information about the running instance
     * Headers    - HTTP headers
     * Parameters - HTTP parameters
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public void writeTo(Viewable viewable, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException,
            WebApplicationException {

        String resolvedPath = viewable.getTemplateName();
        Object model = viewable.getModel();

        LOG.debug("Evaluating freemarker template (" + resolvedPath + ") with model of type " +
                (model == null ? "null" : model.getClass().getSimpleName()));

        // Build the model context that will be passed to the page
        final Map<String, Object> vars;
        if (model instanceof Map) {
            vars = new HashMap<>((Map<String, Object>) model);
        } else {
            vars = new HashMap<>();
            vars.put("it", model);
        }

        RequestContext requestContext = new RequestContext();
        HttpServletRequest httpServletRequest = requestInvoker != null ? requestInvoker.get() : null;
		requestContext.setHttpServletRequest(httpServletRequest );
        vars.put("RequestContext", requestContext);
        vars.put("Request", httpServletRequest);
        if (httpServletRequest != null && viewable.getResolvingClass() != null) {
        	httpServletRequest.setAttribute(ViewableResourceTemplateLoader.KEY_NETFLIX_ADMIN_REQUEST_VIEWABLE, viewable);
        }
        
        Principal ctx = null;
        if (httpServletRequest != null) {
            ctx = httpServletRequest.getUserPrincipal();
            if (ctx == null && httpServletRequest.getSession(false) != null) {
                final String username = (String) httpServletRequest.getSession().getAttribute("SSO_UserName");
                if (username != null) {
                    ctx = new Principal() {
                        @Override
                        public String getName() {
                            return username;
                        }
                    };
                }
            }
        }
        vars.put("Principal", ctx);

        // The following are here for backward compatibility and should be deprecated as soon as possible
        Map<String, Object> global = Maps.newHashMap();
        if (manager != null) {
            GlobalModelContext globalModel = manager.getGlobalModel();
            global.put("sysenv", globalModel.getEnvironment());      // TODO: DEPRECATE
            vars.put("Explorer", manager.getExplorer(AdminExplorerManager.ADMIN_EXPLORER_NAME));
        }
        vars.put("global", global);                            // TODO: DEPRECATE
        vars.put("pathToRoot", requestContext.getPathToRoot());    // TODO: DEPRECATE

        final StringWriter stringWriter = new StringWriter();

        try {
            if (requestContext.getIsAjaxRequest()) {
                fmConfig.getTemplate(resolvedPath).process(vars, stringWriter);
            } else {
                vars.put("nestedpage", resolvedPath);
                fmConfig.getTemplate("/layout/" + ADMIN_CONSOLE_LAYOUT + "/main.ftl").process(vars, stringWriter);
            }
            final OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(stringWriter.getBuffer().toString());
            writer.flush();
        } catch (Throwable t) {
            LOG.error("Error processing freemarker template @ " + resolvedPath + ": " + t.getMessage(), t);
            throw new WebApplicationException(t, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}


