package netflix.adminresources.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import netflix.adminresources.resources.model.Property;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;


@Path("/allprops")
@Produces(MediaType.APPLICATION_JSON)
public class AllPropsResource {

    public static class PropsResponse {
        private Map<String, String> props;

        public PropsResponse(Map<String, String> props) {
            this.props = props;
        }

        public Map<String, String> getProps() {
            return props;
        }
    }

    @GET
    public Response getAllProperties() {
        final List<Property> allProperties = PropertiesHelper.getAllProperties();
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String propsJson = gson.toJson(new PropsResponse(PropertiesHelper.buildPropertiesMap(allProperties)));
        return Response.ok(propsJson).build();
    }
}
