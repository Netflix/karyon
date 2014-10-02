package com.netflix.adminresources.resources;

import com.google.inject.Inject;
import com.netflix.adminresources.AdminPageInfo;
import com.netflix.adminresources.AdminPageRegistry;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;
import com.sun.jersey.api.view.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/admin")
@Produces(MediaType.TEXT_HTML)
public class AdminPageResource {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPageResource.class);

    @Inject
    private AdminPageRegistry adminPageRegistry;


    @GET()
    public Viewable showIndex() {
        Map<String, Object> model = new HashMap<String, Object>();

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
        model.put("instance_hostname", getInstanceHostName(id));
        return new Viewable("/webadmin/" + view + "/index.ftl", model);
    }

    private String getInstanceHostName(String id) {
        if (id != null && !id.isEmpty()) {
            List<InstanceInfo> instances = DiscoveryManager.getInstance().getDiscoveryClient().getInstancesById(id);
            if (instances == null || instances.isEmpty())
                throw new WebApplicationException(new Exception("Hostname for instance " + id + " not found"), 404);
            return instances.get(0).getHostName();
        }
        return "";
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
        model.put("instance_hostname", getInstanceHostName(id));
        return new Viewable("/webadmin/" + view + "/create.ftl", model);
    }

    @GET
    @Path("/jmx/mbean")
    public Viewable showJMXMbean(@QueryParam("id") String id, @QueryParam("key") String key) {
        LOG.info("");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("id", id);
        model.put("key", key);
        //model.put("instance_hostname", "");
        return new Viewable("/webadmin/jmx/view.ftl", model);
    }
}
