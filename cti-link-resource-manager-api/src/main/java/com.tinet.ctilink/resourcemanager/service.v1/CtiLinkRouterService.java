package com.tinet.ctilink.resourcemanager.service.v1;

import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.model.Router;
import com.tinet.ctilink.resourcemanager.response.RouterResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by nope-J on 2016/5/30.
 */

@Path("v1/router")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CtiLinkRouterService {

    @POST
    @Path("list")
    ApiResult<List<RouterResponse>> listRouter(Router router);

    @POST
    @Path("create")
    ApiResult<RouterResponse> createRouter(Router router);

    @POST
    @Path("update")
    ApiResult<RouterResponse> updateRouter(Router router);

    @POST
    @Path("delete")
    ApiResult deleteRouter(Router router);

}
