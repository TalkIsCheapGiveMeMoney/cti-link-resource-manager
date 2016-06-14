package com.tinet.ctilink.resourcemanager.service.v1;

import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.model.SipProxy;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by nope-J on 2016/5/30.
 */

@Path("v1/sipProxy")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CtiLinkSipProxyService {

    @POST
    @Path("list")
    ApiResult<List<SipProxy>> listSipProxy();

    @POST
    @Path("create")
    ApiResult<SipProxy> createSipProxy(SipProxy sipProxy);

    @POST
    @Path("update")
    ApiResult<SipProxy> updateSipProxy(SipProxy sipProxy);

    @POST
    @Path("delete")
    ApiResult deleteSipProxy(SipProxy sipProxy);

}
