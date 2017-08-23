package netflix.adminresources.pages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.view.Viewable;
import netflix.admin.AdminConfigImpl;
import netflix.admin.AdminContainerConfig;
import netflix.adminresources.AdminPageInfo;
import netflix.adminresources.AdminPageRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.TEXT_HTML)
@Singleton
public class AdminPageResource {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPageResource.class);

    @Inject(optional = true)
    private AdminContainerConfig adminContainerConfig;

    @Inject(optional = true)
    private AdminPageRegistry adminPageRegistry;

    @PostConstruct
    public void init() {
        if (adminPageRegistry == null) {
            adminPageRegistry = new AdminPageRegistry();
        }
        if (adminContainerConfig == null) {
            adminContainerConfig = new AdminConfigImpl();
        }
    }

    @GET()
    public Viewable showIndex() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("ajax_base", adminContainerConfig.ajaxDataResourceContext());
        model.put("template_base", adminContainerConfig.templateResourceContext());

        if (adminPageRegistry != null) {
            final Collection<AdminPageInfo> adminPages = adminPageRegistry.getAllPages();
            model.put("adminPages", adminPages);
        }
        if (adminContainerConfig != null) {
            model.put("homeScriptResources", adminContainerConfig.homeScriptResources());
        }

        return new Viewable("/webadmin/home.ftl", model, adminContainerConfig.getClass());
    }

    @GET
    @Path("/{view}")
    public Viewable showViewIndex(
            @PathParam("view") String view,
            @QueryParam("id") @DefaultValue("") String id
    ) {
        LOG.info(view);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("id", id);
        model.put("ajax_base", adminContainerConfig.ajaxDataResourceContext());
        model.put("template_base", adminContainerConfig.templateResourceContext());

		if (adminPageRegistry != null && adminPageRegistry.getPageInfo(view) != null) {
	        AdminPageInfo pageInfo = adminPageRegistry.getPageInfo(view);
            final Map<String, Object> pageDataModel = pageInfo.getDataModel();
            if (pageDataModel != null) {
                model.putAll(pageDataModel);
            }
            return new Viewable(pageInfo.getPageTemplate(), model, pageInfo.getClass());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @POST
    @Path("/{view}")
    public Viewable createObject(
            @PathParam("view") String view,
            @QueryParam("id") @DefaultValue("") String id
    ) {
        LOG.info(view);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("id", id);
        return new Viewable("/webadmin/" + view + "/create.ftl", model, adminContainerConfig.getClass());
    }

    @GET
    @Path("/jmx/mbean")
    public Viewable showJMXMbean(@QueryParam("id") String id, @QueryParam("key") String key) {
        LOG.info("");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("id", id);
        model.put("key", key);
        model.put("ajax_base", adminContainerConfig.ajaxDataResourceContext());

        return new Viewable("/webadmin/jmx/view.ftl", model, adminContainerConfig.getClass());
    }
}
