package com.tinet.ctilink.resourcemanager.service.imp;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.filter.AfterReturningMethod;
import com.tinet.ctilink.resourcemanager.filter.ProviderFilter;
import com.tinet.ctilink.resourcemanager.model.SipGroup;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkSipGroupService;
import com.tinet.ctilink.service.BaseService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nope-J on 2016/5/30.
 */
@Service
public class SipGroupServiceImp extends BaseService<SipGroup> implements CtiLinkSipGroupService{

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisService redisService;

    @Override
    public ApiResult<SipGroup> createSipGroup(SipGroup sipGroup) {
        if(StringUtils.isEmpty(sipGroup.getDescription())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "description不能为空");
        }

        sipGroup.setPercent(0);

        int success = insertSelective(sipGroup);
        if(success == 1){
            setRefreshCacheMethod("setCache",sipGroup);
            return new ApiResult<>(sipGroup);
        }

        logger.error("SipGroupServiceImp.createSipGoup error " + sipGroup + "success="+success);
        return new ApiResult<>(ApiResult.FAIL_RESULT,"新增Sip-Group失败");

    }

    @Override
    public ApiResult deleteSipGroup(SipGroup sipGroup) {
        if(sipGroup.getId() == null || sipGroup.getId() <= 0){
            return new ApiResult(ApiResult.FAIL_RESULT,"id不正确");
        }

        SipGroup sipGroup1 = selectByPrimaryKey(sipGroup.getId());
        if (sipGroup == null) {
            return new ApiResult(ApiResult.FAIL_RESULT, "不存在此id");
        }

        if(sipGroup1.getPercent() != 0) {
            return new ApiResult(ApiResult.FAIL_RESULT, "承压百分比不为0，不能删除");
        }

        int success = deleteByPrimaryKey(sipGroup.getId());
        if(success == 1){
            setRefreshCacheMethod("deleteCache",sipGroup);
            return new ApiResult(ApiResult.SUCCESS_RESULT,ApiResult.SUCCESS_DESCRIPTION);
        }

        logger.error("SipGroupServiceImp.deleteSipGroup error, "+sipGroup+"success="+success);
        return new ApiResult(ApiResult.FAIL_RESULT,"删除失败");
    }

    @Override
    public ApiResult<SipGroup> updateSipGroup(List<SipGroup> sipGroupList)
    {
        int percent = 0;
        for (int i=0;i<sipGroupList.size();i++) {
            SipGroup sipGroup = sipGroupList.get(i);
            if(sipGroup.getId() == null || sipGroup.getId() <= 0){
                return new ApiResult<>(ApiResult.FAIL_RESULT,"存在不正确的id项");
            }

            SipGroup sipGroup1 = selectByPrimaryKey(sipGroup.getId());
            if(sipGroup1 == null ){
                return new ApiResult<>(ApiResult.FAIL_RESULT,"不存在项目："+sipGroupList.get(i).getId());
            }

            sipGroup.setCreateTime(sipGroup1.getCreateTime());

            if(sipGroup.getPercent() == null || sipGroup.getPercent() < 0 || sipGroup.getPercent() > 100){
                return new ApiResult<>(ApiResult.FAIL_RESULT,"存在项目承压百分比不正确");
            }

            if(StringUtils.isEmpty(sipGroup.getDescription())){
                return new ApiResult<>(ApiResult.FAIL_RESULT,"说明不能为空");
            }
            percent += sipGroupList.get(i).getPercent();
        }

        if(percent != 100){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"承压百分比之和不等于100");
        }

        int success = 1;
        for (int i=0;i<sipGroupList.size();i++){
            success = updateByPrimaryKey(sipGroupList.get(i));
            if(success != 1){
                return new ApiResult<>(ApiResult.FAIL_RESULT,"更新失败");
            }
        }

        if(success == 1){
            List<SipGroup> list = selectAll();
            for(int i=0;i<list.size();i++){
                setRefreshCacheMethod("updateCache",list.get(i));
            }
            return new ApiResult<>(ApiResult.SUCCESS_RESULT,ApiResult.SUCCESS_DESCRIPTION);
        }
        logger.error("CtiLinkSopGroupServiceImp.updateSipGroup error, "+sipGroupList+"success=" +success);
        return new ApiResult<>(ApiResult.FAIL_RESULT,"更新失败");
    }

    @Override
    public ApiResult<List<SipGroup>> listSipGroup() {
        List<SipGroup> list = selectAll();
        if(list != null){
            return new ApiResult<>(list);
        }
        return new ApiResult<>(ApiResult.FAIL_RESULT,"获取列表失败");
    }

    public String getKey(){
        return String.format(CacheKey.SIP_GROUP);
    }

    public void setCache(SipGroup sipGroup){
        List<SipGroup> list = redisService.getList(Const.REDIS_DB_CONF_INDEX,getKey(),SipGroup.class);
        if(list == null){
            list = new ArrayList<SipGroup>();
        }

        list.add(sipGroup);
        redisService.set(Const.REDIS_DB_CONF_INDEX,getKey(),list);
    }

    public void updateCache(List<SipGroup> list){
        redisService.delete(Const.REDIS_DB_CONF_INDEX,getKey());
        redisService.set(Const.REDIS_DB_CONF_INDEX,getKey(),list);
    }

    public void deleteCache(SipGroup sipGroup){
        List<SipGroup> list = redisService.getList(Const.REDIS_DB_CONF_INDEX,getKey(),SipGroup.class);
        if(list != null){
            for(int i=0;i<list.size();i++){
                if(sipGroup.getId().equals(list.get(i).getId()) && list.get(i).getPercent() == 0){
                    list.remove(i);
                    break;
                }
            }
        }
        redisService.set(Const.REDIS_DB_CONF_INDEX,getKey(),list);
    }

    private void setRefreshCacheMethod(String methodName, SipGroup sipGroup){
        try{
            Method method = this.getClass().getMethod(methodName,SipGroup.class);
            AfterReturningMethod afterReturningMethod = new AfterReturningMethod(method,this,sipGroup);
            ProviderFilter.LOCAL_METHOD.set(afterReturningMethod);
        }catch (Exception e){
            logger.error("SipGroupServiceImp setRefreshCacheMethod error, refresh cache fail, class = "+ this.getClass().getName() );
        }
    }

}
