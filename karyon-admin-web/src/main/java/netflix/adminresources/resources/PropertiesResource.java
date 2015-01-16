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

package netflix.adminresources.resources;

import com.google.common.annotations.Beta;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import netflix.adminresources.tableview.DataTableHelper;
import netflix.adminresources.tableview.PropsTableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Beta
@Path("/archprops")
@Produces(MediaType.APPLICATION_JSON)
public class PropertiesResource {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesResource.class);

    @Inject(optional = true)
    private PropsTableView adminPropsResource;

    @GET
    public Response getProperties(@Context UriInfo uriInfo) {
        if (adminPropsResource != null) {
            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            JsonObject output = DataTableHelper.buildOutput(adminPropsResource, queryParams);
            return Response.ok().entity(new Gson().toJson(output)).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
