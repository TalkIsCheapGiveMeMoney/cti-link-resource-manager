package com.tinet.ctilink.resourcemanager.service.imp;

import com.alibaba.dubbo.config.annotation.Service;

import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.mapper.EnterpriseIvrMapper;
import com.tinet.ctilink.resourcemanager.mapper.PublicMohMapper;
import com.tinet.ctilink.resourcemanager.model.EnterpriseIvr;
import com.tinet.ctilink.resourcemanager.model.PublicMoh;
import com.tinet.ctilink.resourcemanager.model.PublicVoice;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkPublicVoiceService;
import com.tinet.ctilink.resourcemanager.util.VoiceFile;
import com.tinet.ctilink.service.BaseService;
import com.tinet.ctilink.util.FileUtils;
import com.tinet.ctilink.util.SqlUtil;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Condition;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by nope-J on 2016/6/1.
 */
@Service
public class PublicVoiceServiceImp extends BaseService<PublicVoice> implements CtiLinkPublicVoiceService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PublicMohMapper publicMohMapper;

    @Autowired
    private EnterpriseIvrMapper enterpriseIvrMapper;

    @Override
    public ApiResult<List<PublicVoice>> listPublicVoice() {
        List<PublicVoice> list = selectAll();
        if(list == null || list.size() <= 0){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"获取公共语音列表失败");
        }

        return new ApiResult<>(list);
    }

    @Override
    public ApiResult<PublicVoice> createPublicVoice(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        String voiceName = null;
        List<InputPart> inputParts = uploadForm.get("voiceName");
        System.out.println("------------"+inputParts);
        if(inputParts != null){
            InputPart inputPart = inputParts.get(0);
            try{
                voiceName = inputPart.getBodyAsString();
                System.out.println("-------------------"+voiceName);
            }catch (IOException e){
                logger.error("PublicVoiceServiceImp.createPublicVoice error", e);
            }
        }
        if(StringUtils.isEmpty(voiceName)){
           return new ApiResult<>(ApiResult.FAIL_RESULT,"voiceName is error");
        }

        String description = null;
        inputParts = uploadForm.get("description");
        if (inputParts != null){
            InputPart inputPart = inputParts.get(0);
            try{
                description = inputPart.getBodyAsString();
                System.out.println("----------------"+description);
            }catch(IOException e){
                logger.error("PublicVoiceServiceImp.createPublicVoice error", e);
            }
        }

        inputParts = uploadForm.get("file");
        File file = null;
        if(inputParts != null){
            InputPart inputPart = inputParts.get(0);
            try{
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                file = inputPart.getBody(File.class, null);
                System.out.println("-----------"+file.getName()+"--"+file.getAbsolutePath());
            }catch (IOException e){
                logger.error("PublicVoiceServiceImp.createPublicVoice error", e);
            }
        }

        if(file == null){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"文件参数不正确");
        }

        PublicVoice publicVoice = new PublicVoice();
        publicVoice.setVoiceName(voiceName);
        if(description != null){
            publicVoice.setDescription(description);
        }

        return createPublicVoice(file, publicVoice);
    }

    @Override
    public ApiResult<PublicVoice> createPublicVoice(File file, PublicVoice publicVoice) {
        if(StringUtils.isEmpty(publicVoice.getVoiceName())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"参数[voiceName]不能为空");
        }
        publicVoice.setVoiceName(SqlUtil.escapeSql(publicVoice.getVoiceName()));

        boolean success = false;
        if(file == null){
            if((publicVoice.getVoiceName().startsWith("[自助录音]"))){
                success = true;
            }else{
                return new ApiResult<>(ApiResult.FAIL_RESULT,"参数[file]不能为空");
            }
        }else{
            Long timestamp = new Date().getTime();
            String uploadPath = Const.SOUNDS_IVR_VOICE_ABS_PATH;
            String srcFile = timestamp + "old.mav";
            System.out.println(file.getName());
            String destFile = timestamp + ".wav";
            System.out.println(srcFile+"-------------");
            System.out.println(destFile+"-----------------");

            if(VoiceFile.mkDir(uploadPath)){
                System.out.println("success---------------------------------------");
                FileUtils.moveFile(file, srcFile, uploadPath);
                success = true;//VoiceFile.transferPublicVoice(srcFile,destFile);
            }

            //publicVoice.setPath(destFile);
            publicVoice.setPath(srcFile);
            publicVoice.setDescription(SqlUtil.escapeSql(publicVoice.getDescription()));
        }

        if(success){
            int count = insertSelective(publicVoice);
            if(count != 1){
                logger.error("PublicVoiceServiceImp.createPublicVoice error, " + publicVoice + ", count = " + count);
                return new ApiResult<>(ApiResult.FAIL_RESULT,"新增失败++++++++++");
            }else{
                return new ApiResult<>(publicVoice);
            }
        }else{
            return new ApiResult<>(ApiResult.FAIL_RESULT,"新增失败-----------");
        }
    }

    @Override
    public ApiResult<PublicVoice> updatePublicVoice(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        int id = -1;
        List<InputPart> inputParts = uploadForm.get("id");
        if(inputParts != null){
            InputPart inputPart = inputParts.get(0);
            try{
                id = Integer.parseInt(inputPart.getBodyAsString());
            }catch (IOException e){
                logger.error("PublicVoiceServiceImp.updatePublicVoice error", e);
            }
        }
        PublicVoice publicVoice1 = selectByPrimaryKey(id);
        if(publicVoice1 == null){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"不存在此id的公共语音");
        }

        String voiceName = null;
        inputParts = uploadForm.get("voiceName");
        if(inputParts != null){
            InputPart inputPart = inputParts.get(0);
            try{
                voiceName = inputPart.getBodyAsString();
            }catch (IOException e){
                logger.error("PublicVoiceServiceImp.updatePublicVoice error", e);
            }
        }
        if(StringUtils.isEmpty(voiceName)){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"公共语音名称不正确");
        }

        String description = null;
        inputParts = uploadForm.get("description");
        if (inputParts != null){
            InputPart inputPart = inputParts.get(0);
            try{
                description = inputPart.getBodyAsString();
            }catch(IOException e){
                logger.error("PublicVoiceServiceImp.updatePublicVoice error", e);
            }
        }

        inputParts = uploadForm.get("file");
        File file = null;
        if(inputParts != null){
            InputPart inputPart = inputParts.get(0);
            try{
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                file = inputPart.getBody(File.class, null);
            }catch (IOException e){
                logger.error("PublicVoiceServiceImp.updatePublicVoice error", e);
            }
        }

        if(file == null){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"文件参数不正确");
        }

        PublicVoice publicVoice = new PublicVoice();
        publicVoice.setVoiceName(voiceName);
        if(description != null){
            publicVoice.setDescription(description);
        }
        publicVoice.setCreateTime(publicVoice1.getCreateTime());
        return updatePublicVoice(file, publicVoice);
    }

    @Override
    public ApiResult<PublicVoice> updatePublicVoice(File file, PublicVoice publicVoice) {
        if (publicVoice.getId() == null || publicVoice.getId() <= 0){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"参数[id]不正确");
        }
        PublicVoice publicVoice1 = selectByPrimaryKey(publicVoice.getId());
        if(publicVoice1 == null){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"参数[id]不存在");
        }

        if (isUseInMoh(publicVoice1.getId())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "已被等待音乐设置，不能更新");
        }
        if (isUseInIvr(publicVoice)) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "在语音导航中使用，不能更新");
        }

        if (StringUtils.isEmpty(publicVoice.getVoiceName())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "参数[voiceName不能为空]");
        }
        publicVoice1.setVoiceName(SqlUtil.escapeSql(publicVoice.getVoiceName()));

        long timestamp = new Date().getTime();
        String uploadPath = Const.SOUNDS_IVR_VOICE_ABS_PATH;
        String srcFile = timestamp + "old.wav";
        String destFile = timestamp + ".wav";

        String oldPath = publicVoice1.getPath();
        boolean success = false;
        if (VoiceFile.mkDir(uploadPath)) {
            FileUtils.moveFile(file, srcFile, uploadPath);
            success = true;//VoiceFile.transferPublicVoice(srcFile, destFile);
        }

        if (success) {
            //publicVoice1.setPath(destFile);
            publicVoice1.setPath(srcFile);
            publicVoice1.setDescription(SqlUtil.escapeSql(publicVoice.getDescription()));

            int count = updateByPrimaryKeySelective(publicVoice1);
            if (count != 1) {
                logger.error("PublicVoiceServiceImp.updatePublicVoice error, " + publicVoice1 + ", count=" + count);
                return new ApiResult<>(ApiResult.FAIL_RESULT, "更新失败");
            } else {
                VoiceFile.deleteEnterpriseVoice(oldPath);
                return new ApiResult<>(publicVoice1);
            }
        } else {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "更新失败");
        }
    }

    @Override
    public ApiResult deletePublicVoice(PublicVoice publicVoice) {
        if (publicVoice.getId() == null || publicVoice.getId() <= 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "参数[id]不正确");
        }

        PublicVoice publicVoice1 = selectByPrimaryKey(publicVoice.getId());
        if (publicVoice1 == null) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "参数[id]不存在");
        }

        if (isUseInMoh(publicVoice.getId())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "已被等待音乐设置，不能删除");
        }
        if (isUseInIvr(publicVoice)) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "在语音导航中使用，不能删除");
        }
        boolean success = VoiceFile.deleteEnterpriseVoice(publicVoice1.getPath());

        int count = deleteByPrimaryKey(publicVoice.getId());

        if (count != 1) {
            logger.error("PublicVoiceServiceImp.deleteEnterpriseVoice error, " + publicVoice + ", count=" + count);
            return new ApiResult<>(ApiResult.FAIL_RESULT, "删除失败");
        }
        return new ApiResult(ApiResult.SUCCESS_RESULT);
    }

    private boolean isUseInMoh(Integer voiceId) {
        PublicMoh publicMoh = publicMohMapper.selectByPrimaryKey(voiceId);
        if(publicMoh != null){
            return  true;
        }

        return false;
    }

    private boolean isUseInIvr(PublicVoice publicVoice) {
        Condition condition = new Condition(EnterpriseIvr.class);
        Condition.Criteria criteria = condition.createCriteria();
        List<Integer> actionList = new ArrayList<>();
        actionList.add(Const.ENTERPRISE_IVR_OP_ACTION_PLAY);
        actionList.add(Const.ENTERPRISE_IVR_OP_ACTION_SELECT);
        actionList.add(Const.ENTERPRISE_IVR_OP_ACTION_READ);
        actionList.add(Const.ENTERPRISE_IVR_OP_ACTION_DIAL);
        criteria.andIn("action", actionList);
        List<EnterpriseIvr> list = enterpriseIvrMapper.selectByCondition(condition);
        if (list != null && list.size() > 0) {
            String path = publicVoice.getPath().substring(0, publicVoice.getPath().lastIndexOf("."));
            for (EnterpriseIvr enterpriseIvr : list) {
                if (enterpriseIvr.getProperty().contains(path)) {
                    return true;
                }
            }
        }
        return false;
    }

}
