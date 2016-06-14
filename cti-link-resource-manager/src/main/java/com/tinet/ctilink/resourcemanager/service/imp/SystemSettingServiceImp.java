package com.tinet.ctilink.resourcemanager.service.imp;

import com.alibaba.dubbo.config.annotation.Service;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.ApiResult;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.filter.AfterReturningMethod;
import com.tinet.ctilink.resourcemanager.filter.ProviderFilter;
import com.tinet.ctilink.conf.model.SystemSetting;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkSystemSettingService;
import com.tinet.ctilink.service.BaseService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by nope-J on 2016/5/31.
 */
@Service
public class SystemSettingServiceImp extends BaseService<SystemSetting> implements CtiLinkSystemSettingService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisService redisService;

    @Override
    public ApiResult<List<SystemSetting>> listSystemSetting() {
        List<SystemSetting> list = selectAll();
        if (list == null || list.size() <= 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "获取列表失败");
        }

        return new ApiResult<>(list);
    }

    @Override
    public ApiResult updateSystemSetting(SystemSetting systemSetting) {
        if (systemSetting.getId() == null || systemSetting.getId() < 0) {
            return new ApiResult(ApiResult.FAIL_RESULT, "id不正确");
        }

        SystemSetting systemSetting1 = selectByPrimaryKey(systemSetting.getId());
        if (systemSetting1 == null) {
            return new ApiResult(ApiResult.FAIL_RESULT, "不存在id:" + systemSetting.getId());
        }

        if (StringUtils.isEmpty(systemSetting.getName())) {
            return new ApiResult(ApiResult.FAIL_RESULT, "名称不能为空");
        }

        if (StringUtils.isEmpty(systemSetting.getValue())) {
            return new ApiResult(ApiResult.FAIL_RESULT, "值不能为空");
        }

        if (StringUtils.isEmpty(systemSetting.getProperty())) {
            return new ApiResult(ApiResult.FAIL_RESULT, "属性不能为空");
        }

        systemSetting.setCreateTime(systemSetting1.getCreateTime());

        int success = updateByPrimaryKey(systemSetting);
        if (success == 1) {
            setRefreshCacheMethod("setCache", systemSetting);
            return new ApiResult(success);
        }

        logger.error("SystemSettingServiceImp.updateSystemSetting error, " + systemSetting + "success = " + success);
        return new ApiResult(ApiResult.FAIL_RESULT, "更新失败");
    }

    public String getKey(SystemSetting systemSetting) {
        return String.format(CacheKey.SYSTEM_SETTING_NAME, systemSetting.getName());
    }

    public void setCache(SystemSetting systemSetting) {
        redisService.set(Const.REDIS_DB_CONF_INDEX, getKey(systemSetting), systemSetting);
    }

    private void setRefreshCacheMethod(String methodName, SystemSetting systemSetting) {
        try {
            Method method = this.getClass().getMethod(methodName, SystemSetting.class);
            AfterReturningMethod afterReturningMethod = new AfterReturningMethod(method, this, systemSetting);
            ProviderFilter.LOCAL_METHOD.set(afterReturningMethod);
        } catch (Exception e) {
            logger.error("SystemSettingServiceImp setRefreshCacheMethod error, fail to refresh cache, class=" + this.getClass().getName());
        }
    }
}
