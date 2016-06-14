package com.tinet.ctilink.resourcemanager.service.imp;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.ApiResult;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.filter.AfterReturningMethod;
import com.tinet.ctilink.resourcemanager.filter.ProviderFilter;
import com.tinet.ctilink.conf.model.SipProxy;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkSipProxyService;
import com.tinet.ctilink.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nope-J on 2016/5/31.
 */
@Service
public class SipProxyServiceImp extends BaseService<SipProxy> implements CtiLinkSipProxyService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisService redisService;

    @Override
    public ApiResult<List<SipProxy>> listSipProxy() {
        List<SipProxy> list = selectAll();
        if (list != null && list.size() > 0) {
            return new ApiResult<>(list);
        }

        return new ApiResult<>(ApiResult.FAIL_RESULT, "获取列表失败");
    }

    @Override
    public ApiResult<SipProxy> createSipProxy(SipProxy sipProxy) {
        ApiResult<SipProxy> result = validateSipProxy(sipProxy);
        if (result != null) {
            return result;
        }

        sipProxy.setCreateTime(new Date());

        int success = insertSelective(sipProxy);
        if (success == 1) {
            setRefreshCacheMethod("setCache", sipProxy);
            return new ApiResult<>(sipProxy);
        }

        logger.error("CtiLinkSipProxyServiceImo.createSipProxy error," + sipProxy + "success=" + success);
        return new ApiResult<>(ApiResult.FAIL_RESULT, "新增失败");
    }

    @Override
    public ApiResult<SipProxy> updateSipProxy(SipProxy sipProxy) {
        if (sipProxy.getId() == null || sipProxy.getId() <= 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "id不正确");
        }
        SipProxy sipProxy1 = selectByPrimaryKey(sipProxy.getId());
        if (sipProxy1 == null) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "不存在id:" + sipProxy.getId());
        }

        ApiResult<SipProxy> result = validateSipProxy(sipProxy);
        if (result != null) {
            return result;
        }
        sipProxy.setCreateTime(sipProxy1.getCreateTime());

        int success = updateByPrimaryKey(sipProxy);
        if (success == 1) {
            setRefreshCacheMethod("setCache", sipProxy);
            return new ApiResult<>(sipProxy);
        }

        logger.error("SipProxyServiceImp.updateSipProxy error," + sipProxy + "success=" + success);
        return new ApiResult<>(ApiResult.FAIL_RESULT, "更新失败");
    }

    @Override
    public ApiResult deleteSipProxy(SipProxy sipProxy) {
        if (sipProxy.getId() == null || sipProxy.getId() <= 0) {
            return new ApiResult(ApiResult.FAIL_RESULT, "id不正确");
        }

        SipProxy sipProxy1 = selectByPrimaryKey(sipProxy.getId());
        if (sipProxy1 == null) {
            return new ApiResult(ApiResult.FAIL_RESULT, "不存在此id:" + sipProxy.getId());
        }

        int success = deleteByPrimaryKey(sipProxy.getId());
        if (success == 1) {
            setRefreshCacheMethod("deleteCache", sipProxy1);
            return new ApiResult(ApiResult.SUCCESS_RESULT, ApiResult.SUCCESS_DESCRIPTION);
        }

        logger.error("SipProxyServiceImp.deleteSipProxy error, " + sipProxy + "success=" + success);
        return new ApiResult(ApiResult.FAIL_RESULT, "删除失败");
    }

    public String getKey(SipProxy sipProxy) {
        return String.format(CacheKey.SIP_PROXY_IP_ADDR, sipProxy.getIpAddr());
    }

    public void setCache(SipProxy sipProxy) {
        redisService.set(Const.REDIS_DB_CONF_INDEX, getKey(sipProxy), sipProxy);

        List<SipProxy> list = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SIP_PROXY), SipProxy.class);
        if (list == null) {
            list = new ArrayList<SipProxy>();
        }
        list.add(sipProxy);
        redisService.set(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SIP_PROXY), list);
    }

    public void deleteCache(SipProxy sipProxy) {
        redisService.delete(Const.REDIS_DB_CONF_INDEX, getKey(sipProxy));

        List<SipProxy> list = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SIP_PROXY), SipProxy.class);
        for (SipProxy sipProxy1 : list) {
            if (sipProxy1.getIpAddr().equals(sipProxy.getIpAddr())) {
                list.remove(sipProxy1);
                break;
            }
        }
        redisService.set(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SIP_PROXY), list);


    }

    private void setRefreshCacheMethod(String methodName, SipProxy sipProxy) {
        try {
            Method method = this.getClass().getMethod(methodName, SipProxy.class);
            AfterReturningMethod afterReturningMethod = new AfterReturningMethod(method, this, sipProxy);
            ProviderFilter.LOCAL_METHOD.set(afterReturningMethod);
        } catch (Exception e) {
            logger.error("SipProxyServiceImp setRefreshCacheMethod error, refresh cache fail, class=" + this.getClass().getName(), e);
        }
    }

    private <T> ApiResult<T> validateSipProxy(SipProxy sipProxy) {
        if (StringUtils.isEmpty(sipProxy.getName())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "名称不能为空");
        }

        if (StringUtils.isEmpty(sipProxy.getIpAddr())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "ip地址不能为空");
        }

        if (sipProxy.getPort() == null) {
            new ApiResult<>(ApiResult.FAIL_RESULT, "信令端口不能为空");
        }

        if (StringUtils.isEmpty(sipProxy.getDescription())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "说明不能为空");
        }

        if (sipProxy.getActive() == null || !(sipProxy.getActive() == 1 || sipProxy.getActive() == 0)) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "是否激活;1 激活，0.暂停");
        }

        return null;
    }
}
