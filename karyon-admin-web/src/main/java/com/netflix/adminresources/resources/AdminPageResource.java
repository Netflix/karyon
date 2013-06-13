package com.netflix.adminresources.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jersey.api.view.Viewable;

@Path("/admin")
@Produces( MediaType.TEXT_HTML )
public class AdminPageResource {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPageResource.class);
	
	@GET()
    public Viewable showIndex(
    ){
		LOG.info("showIndex");
		Map<String, Object> model = new HashMap<String, Object>();
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
        return new Viewable( "/webadmin/" + view + "/index.ftl", model );
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
        return new Viewable( "/webadmin/" + view + "/create.ftl", model );
	}

    @GET
    @Path("/jmx/mbean")
    public Viewable showJMXMbean(@QueryParam("id") String id, @QueryParam("key") String key) {
        LOG.info("");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("id", id);
        model.put("key", key);
        //model.put("instance_hostname", "");
        return new Viewable( "/webadmin/jmx/view.ftl", model );
    }
}
