package com.tinet.ctilink.resourcemanager.service.v1;

import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.model.Gateway;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.List;

/**
 * Created by nope-J on 2016/5/30.
 */

@Path("v1/gateway")
public interface CtiLinkGatewayService {

    @POST
    @Path("list")
    ApiResult<List<Gateway>> listGateway(Gateway gateway);

    @POST
    @Path("create")
    ApiResult<Gateway> createGateway(Gateway gateway);

    @POST
    @Path("update")
    ApiResult<Gateway> updateGateway(Gateway gateway);

    @POST
    @Path("delete")
    ApiResult deleteGateway(Gateway gateway);

}
