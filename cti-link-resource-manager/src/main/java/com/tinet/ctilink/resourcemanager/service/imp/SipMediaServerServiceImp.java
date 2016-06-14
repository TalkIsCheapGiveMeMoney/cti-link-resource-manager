package com.tinet.ctilink.resourcemanager.service.imp;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.filter.AfterReturningMethod;
import com.tinet.ctilink.resourcemanager.filter.ProviderFilter;
import com.tinet.ctilink.resourcemanager.mapper.SipGroupMapper;
import com.tinet.ctilink.resourcemanager.model.SipGroup;
import com.tinet.ctilink.resourcemanager.model.SipMediaServer;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkSipMediaServerService;
import com.tinet.ctilink.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Condition;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by nope-J on 2016/6/1.
 */
@Service
public class SipMediaServerServiceImp extends BaseService<SipMediaServer> implements CtiLinkSipMediaServerService {
    private Logger logger = LoggerFactory.getLogger(getClass());

   @Autowired
   private RedisService redisService;

    @Autowired
    private SipGroupMapper sipGroupMapper;

    @Override
    public ApiResult<SipMediaServer> createSipMediaServer(SipMediaServer sipMediaServer){
        if(StringUtils.isEmpty(sipMediaServer.getIpAddr())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"内网ip地址不能为空");
        }

        if(StringUtils.isEmpty(sipMediaServer.getExternalIpAddr())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"公网ip地址不能为空");
        }

        if (StringUtils.isEmpty(sipMediaServer.getMac())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT,"mac地址不能为空");

        }

        if(StringUtils.isEmpty(sipMediaServer.getInstanceId())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"实例id不能为空");
        }

        if(sipMediaServer.getStatus() == null || !(sipMediaServer.getStatus() == 0 || sipMediaServer.getStatus() == 1
                || sipMediaServer.getStatus() == 2 || sipMediaServer.getStatus() == 3 || sipMediaServer.getStatus() ==4)){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"实例状态 0:启动中 1:正常 2:销毁中 3:升级中 4:不可用");
        }

        List<SipMediaServer> list = selectAll();
        int[] sipIds = new int[list.size()];
        for (int i=0;i<list.size();i++){
            sipIds[i] = list.get(i).getSipId();
        }
        Arrays.sort(sipIds);

        int sipId = 1;
        for(int i=0;i<sipIds.length;i++) {
            if (sipId == sipIds[i]) {
                sipId++;
            } else
                break;
        }
        sipMediaServer.setSipId(sipId);

        sipMediaServer.setGroupId(1);

        sipMediaServer.setDescription("");

        sipMediaServer.setCreateTime(new Date());

        int success = insertSelective(sipMediaServer);
        if(success == 1){
            setRefreshCacheMethod("setCache",sipMediaServer);
            return new ApiResult<>(sipMediaServer);
        }

        logger.error("SipMediaServerServiceImp.createSipMediaServer errror, sipMediaServer = "+ sipMediaServer + ", success = "+success);
        return new ApiResult<>(ApiResult.FAIL_RESULT,"注册失败");
    }

    @Override
    public ApiResult<List<SipMediaServer>> listSipMediaServer() {
        List<SipMediaServer> list = selectAll();
        if(list == null || list.size() <= 0){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"获取列表失败");
        }

        return new ApiResult<>(list);
    }

    @Override
    public ApiResult<SipMediaServer> updateSipMediaServer(SipMediaServer sipMediaServer) {
        if(StringUtils.isEmpty(sipMediaServer.getInstanceId())){
            return new ApiResult<>(ApiResult.FAIL_RESULT, "实例id不能为空");
        }

        if(StringUtils.isEmpty(sipMediaServer.getMac())){
            return new ApiResult<>(ApiResult.FAIL_RESULT, "mac地址不能为空");
        }

        if(sipMediaServer.getSipId() == null || sipMediaServer.getSipId() <= 0){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"sipId不正确");
        }

        if(StringUtils.isEmpty(sipMediaServer.getIpAddr())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"ip地址不能为空");
        }

        if(StringUtils.isEmpty(sipMediaServer.getExternalIpAddr())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"公网ip地址不能为空");
        }

        if(sipMediaServer.getPort() == null || sipMediaServer.getPort() <= 0){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"sip信令端口不正确");
        }

        if(StringUtils.isEmpty(sipMediaServer.getDescription())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"说明不能为空");
        }

        if(sipMediaServer.getStatus() == null){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"当前状态不能为空");
        }

        if(sipMediaServer.getActive() == null || !(sipMediaServer.getActive() ==0 || sipMediaServer.getActive() == 1)){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"是否激活：1.激活  0.暂停");
        }

        if(sipMediaServer.getGroupId() == null || sipMediaServer.getGroupId() <= 0){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"组id不正确");
        }
        SipGroup sipGroup = sipGroupMapper.selectByPrimaryKey(sipMediaServer.getGroupId());
        if(sipGroup == null){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"不存在该组id");
        }

        Condition condition = new Condition(SipMediaServer.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("instanceId", sipMediaServer.getInstanceId());

        SipMediaServer sipMediaServer1 = selectByCondition(condition).get(0);
        if(sipMediaServer1 == null){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"不存在此实例");
        }

        sipMediaServer.setCreateTime(sipMediaServer1.getCreateTime());

        int success = updateByCondition(sipMediaServer,condition);
        if(success == 1){
            setRefreshCacheMethod("setCache",sipMediaServer);
            return new ApiResult<>(sipMediaServer);
        }

        return new ApiResult<>(ApiResult.FAIL_RESULT,"更新失败");
    }

    @Override
    public ApiResult deleteSipMediaServer(SipMediaServer sipMediaServer) {
        if(StringUtils.isEmpty(sipMediaServer.getInstanceId())){
            return new ApiResult(ApiResult.FAIL_RESULT,"实例id不能为空");
        }

        Condition condition = new Condition(SipMediaServer.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("instanceId",sipMediaServer.getInstanceId());

        List<SipMediaServer> sipMediaServerList = selectByCondition(condition);
        if(sipMediaServerList == null || sipMediaServerList.size() <=0){
            return new ApiResult(ApiResult.FAIL_RESULT,"不存在该实例");
        }
        SipMediaServer sipMediaServer1 = sipMediaServerList.get(0);

        int success = deleteByCondition(condition);
        if(success == 1){
            setRefreshCacheMethod("deleteCache",sipMediaServer1);
            return new ApiResult(ApiResult.SUCCESS_RESULT,ApiResult.SUCCESS_DESCRIPTION);
        }

        logger.error("SipMediaServerServiceImp.deleteSipMediaServer error,"+sipMediaServer+"success="+success);
        return new ApiResult(ApiResult.FAIL_RESULT,"删除失败");
    }

    public String getKey(SipMediaServer sipMediaServer){
        return String.format(CacheKey.SIP_MEDIA_SERVER_IP_ADDR,sipMediaServer.getIpAddr());
    }

    public void setCache(SipMediaServer sipMediaServer){
        redisService.set(Const.REDIS_DB_CONF_INDEX,getKey(sipMediaServer),sipMediaServer);
    }

    public void deleteCache(SipMediaServer sipMediaServer){
        redisService.delete(Const.REDIS_DB_CONF_INDEX,getKey(sipMediaServer));
    }

    private void setRefreshCacheMethod(String methodName,SipMediaServer sipMediaServer){
        try{
            Method method = this.getClass().getMethod(methodName,SipMediaServer.class);
            AfterReturningMethod afterReturningMethod = new AfterReturningMethod(method,this,sipMediaServer);
            ProviderFilter.LOCAL_METHOD.set(afterReturningMethod);
        }catch(Exception e) {
            logger.error("SipMediaServerServiceImp.setRefreshCacheMethod error, fail to refresh cache, class = " + this.getClass().getName());
        }
    }
}
