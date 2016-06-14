package com.tinet.ctilink.resourcemanager.service.v1;

import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.model.Routerset;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by nope-J on 2016/5/30.
 */

@Path("v1/routerset")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CtiLinkRoutersetService {

    @POST
    @Path("list")
    ApiResult<List<Routerset>> listRouterSet();

    @POST
    @Path("create")
    ApiResult<Routerset> createRouterset(Routerset routerset);

    @POST
    @Path("update")
    ApiResult<Routerset> updateRouterset(Routerset routerset);

    @POST
    @Path("delete")
    ApiResult deleteRouterset(Routerset routerset);
}
