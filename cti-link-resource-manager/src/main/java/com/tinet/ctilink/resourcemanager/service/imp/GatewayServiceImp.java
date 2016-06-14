package com.tinet.ctilink.resourcemanager.service.imp;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.ApiResult;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.filter.AfterReturningMethod;
import com.tinet.ctilink.resourcemanager.filter.ProviderFilter;
import com.tinet.ctilink.conf.model.Gateway;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkGatewayService;
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
public class GatewayServiceImp extends BaseService<Gateway> implements CtiLinkGatewayService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisService redisService;


    @Override
    public ApiResult<List<Gateway>> listGateway(Gateway gateway) {
        List<Gateway> list = selectAll();
        if (list == null || list.size() <= 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "获取列表失败");
        }

        return new ApiResult<>(list);
    }

    @Override
    public ApiResult<Gateway> createGateway(Gateway gateway) {
        ApiResult<Gateway> result = validateGateway(gateway);
        if (result != null) {
            return result;
        }

        gateway.setCreateTime(new Date());

        int success = insertSelective(gateway);
        if (success == 1) {
            setRefreshCacheMethod("setCache", gateway);
            return new ApiResult<>(gateway);
        }

        logger.error("GatewayServiceImp.createGateway error, gateway = " + gateway + ", success = " + success);
        return new ApiResult<>(ApiResult.FAIL_RESULT, "新增失败");

    }

    @Override
    public ApiResult<Gateway> updateGateway(Gateway gateway) {
        ApiResult<Gateway> result = validateGateway(gateway);
        if (result != null) {
            return result;
        }

        if (gateway.getId() == null || gateway.getId() < 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "id不正确");
        }

        Gateway gateway1 = selectByPrimaryKey(gateway.getId());
        if (gateway1 == null) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "不存在此Id:" + gateway.getId());
        }

        gateway.setCreateTime(gateway1.getCreateTime());

        int success = updateByPrimaryKey(gateway);
        if (success == 1) {
            setRefreshCacheMethod("seCache", gateway);
            return new ApiResult<>(gateway);
        }

        logger.error("GatewayServiceImp.updateGateway error, " + gateway + " success=" + success);
        return new ApiResult<>(ApiResult.FAIL_RESULT, "更新失败");
    }

    @Override
    public ApiResult deleteGateway(Gateway gateway) {
        if (gateway.getId() == null || gateway.getId() < 0) {
            return new ApiResult(ApiResult.FAIL_RESULT, "id不正确");
        }

        Gateway gateway1 = selectByPrimaryKey(gateway.getId());

        int success = deleteByPrimaryKey(gateway.getId());
        if (success == 1) {
            setRefreshCacheMethod("deleteCache", gateway1);
            return new ApiResult(ApiResult.SUCCESS_RESULT, ApiResult.SUCCESS_DESCRIPTION);
        }

        logger.error("GatewayServiceImp.deleteGateway error, " + gateway + " success = " + success);
        return new ApiResult(ApiResult.FAIL_RESULT, "删除失败");
    }

    public String getKey(Gateway gateway) {
        return String.format(CacheKey.GATEWAY_ID, gateway.getId());
    }

    public void setCache(Gateway gateway) {
        redisService.set(Const.REDIS_DB_CONF_INDEX, getKey(gateway), gateway);

        List<Gateway> list = redisService.getList(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, Gateway.class);
        if (list == null) {
            list = new ArrayList<Gateway>();
        }
        list.add(gateway);
        redisService.set(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, list);
    }

    public void deleteCache(Gateway gateway) {
        redisService.delete(Const.REDIS_DB_CONF_INDEX, getKey(gateway));

        List<Gateway> list = redisService.getList(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, Gateway.class);
        for (Gateway gateway1 : list) {
            if (gateway1.getName().equals(gateway.getName())) {
                list.remove(gateway1);
            }
            break;
        }
        redisService.set(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, list);
    }

    private void setRefreshCacheMethod(String methodName, Gateway gateway) {
        try {
            Method method = this.getClass().getMethod(methodName, Gateway.class);
            AfterReturningMethod afterReturningMethod = new AfterReturningMethod(method, this, gateway);
            ProviderFilter.LOCAL_METHOD.set(afterReturningMethod);
        } catch (Exception e) {
            logger.error("GatewayServiceImp setRefreshCacheMethod error, class = " + this.getClass().getName(), e);
        }
    }

    private <T> ApiResult<T> validateGateway(Gateway gateway) {
        if (StringUtils.isEmpty(gateway.getName())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "网关名称不能为空");
        }

        if (StringUtils.isEmpty(gateway.getPrefix())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "号码前缀不能为空");
        }

        if (StringUtils.isEmpty(gateway.getIpAddr())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "ip地址不能为空");
        }

        if (gateway.getPort() == null || gateway.getPort() <= 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "端口不正确");
        }

        if (StringUtils.isEmpty(gateway.getAreaCode())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "网关区号不能为空");
        }

        if (gateway.getCallLimit() == null || gateway.getCallLimit() < 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "网关吞吐能力不正确");
        }

        if (StringUtils.isEmpty(gateway.getDisallow())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "请选择disallow");
        }

        if (StringUtils.isEmpty(gateway.getAllow())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "请选择allow");
        }

        if (StringUtils.isEmpty(gateway.getDtmfMode())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "网关配置不能为空");
        }
        return null;
    }
}
