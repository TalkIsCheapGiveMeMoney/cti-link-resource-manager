package com.tinet.ctilink.resourcemanager.service.v1;

import com.tinet.ctilink.conf.ApiResult;
import com.tinet.ctilink.conf.model.SipMediaServer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by nope-J on 2016/5/30.
 */

@Path("v1/sipMediaServer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CtiLinkSipMediaServerService {

    @POST
    @Path("registry")
    ApiResult<SipMediaServer> createSipMediaServer(SipMediaServer sipMediaServer);

    @POST
    @Path("list")
    ApiResult<List<SipMediaServer>> listSipMediaServer();

    @POST
    @Path("update")
    ApiResult<SipMediaServer> updateSipMediaServer(SipMediaServer sipMediaServer);

    @POST
    @Path("delete")
    ApiResult deleteSipMediaServer(SipMediaServer sipMediaServer);


}
