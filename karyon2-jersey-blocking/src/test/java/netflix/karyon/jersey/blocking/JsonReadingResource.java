package netflix.karyon.jersey.blocking;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

@Path("/test")  
public class JsonReadingResource {
  private final ObjectMapper mapper = new ObjectMapper();
  
  @SuppressWarnings("unused")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response processJson( String payload ) {
    
    try {
      
      System.out.println( "processing payload size: '" + payload.length() + "'" );
      
      JsonNode tree = mapper.readTree( payload );
      
      return Response.ok().build();
    }
    catch( Exception e ) {
      System.err.println( "ERROR:" + e.getMessage() );
      
      return Response.serverError().build();
    }
  }
}          
                     