package netflix.adminresources.pages;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.view.Viewable;
import netflix.admin.AdminContainerConfig;
import netflix.adminresources.AdminPageInfo;
import netflix.adminresources.AdminPageRegistry;
import netflix.adminresources.AdminResourcesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
@Produces(MediaType.TEXT_HTML)
@Singleton
public class AdminPageResource {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPageResource.class);
    private final AdminContainerConfig adminContainerConfig;
    private final AdminPageRegistry adminPageRegistry;

    @Inject
    public AdminPageResource(AdminContainerConfig adminContainerConfig, AdminResourcesContainer adminResourcesContainer) {
        this.adminContainerConfig = adminContainerConfig;
        this.adminPageRegistry = adminResourcesContainer.getAdminPageRegistry();
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

        return new Viewable("/webadmin/home.ftl", model);
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
            final Map<String, Object> pageDataModel = adminPageRegistry.getPageInfo(view).getDataModel();
            if (pageDataModel != null) {
                model.putAll(pageDataModel);
            }
            return new Viewable(adminPageRegistry.getPageInfo(view).getPageTemplate(), model);
        }

        return new Viewable("/webadmin/" + view + "/index.ftl", model);
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
        return new Viewable("/webadmin/" + view + "/create.ftl", model);
    }

    @GET
    @Path("/jmx/mbean")
    public Viewable showJMXMbean(@QueryParam("id") String id, @QueryParam("key") String key) {
        LOG.info("");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("id", id);
        model.put("key", key);
        model.put("ajax_base", adminContainerConfig.ajaxDataResourceContext());

        return new Viewable("/webadmin/jmx/view.ftl", model);
    }
}
