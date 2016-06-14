package com.tinet.ctilink.resourcemanager.service.v1;

import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.model.PublicMoh;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by nope-J on 2016/5/30.
 */

@Path("v1/publicMoh")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CtiLinkPublicMohService {

    @POST
    @Path("update")
    ApiResult updatePublicMoh(PublicMoh publicMoh,int moh);

}
