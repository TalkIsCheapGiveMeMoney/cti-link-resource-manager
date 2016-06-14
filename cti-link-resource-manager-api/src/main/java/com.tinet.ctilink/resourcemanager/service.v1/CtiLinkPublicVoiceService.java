package com.tinet.ctilink.resourcemanager.service.v1;

import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.model.PublicVoice;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;

/**
 * Created by nope-J on 2016/5/30.
 */

@Path("v1/publicVoice")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface CtiLinkPublicVoiceService {

    @POST
    @Path("list")
    ApiResult<List<PublicVoice>> listPublicVoice();

    @POST
    @Path("create")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    ApiResult<PublicVoice> createPublicVoice(MultipartFormDataInput input);

    @POST
    @Path("create/file")
    ApiResult<PublicVoice> createPublicVoice(File file, PublicVoice publicVoice);

    @POST
    @Path("update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    ApiResult<PublicVoice> updatePublicVoice(MultipartFormDataInput input);

    @POST
    @Path("update/file")
    ApiResult<PublicVoice> updatePublicVoice(File file, PublicVoice publicVoice);

    @POST
    @Path("delete")
    ApiResult deletePublicVoice(PublicVoice publicVoice);

}
