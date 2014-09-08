package com.netflix.karyon.ws.rs.test;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/text")
@Produces({MediaType.TEXT_PLAIN})
public class TextResource extends BaseResourceImpl {

}
