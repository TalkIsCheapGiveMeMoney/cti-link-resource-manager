package com.tinet.ctilink.resourcemanager.service.imp;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.ApiResult;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.filter.AfterReturningMethod;
import com.tinet.ctilink.resourcemanager.filter.ProviderFilter;
import com.tinet.ctilink.conf.model.PublicMoh;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkPublicMohService;
import com.tinet.ctilink.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

/**
 * Created by nope-J on 2016/6/6.
 */
public class PublicMohServiceImp extends BaseService<PublicMoh> implements CtiLinkPublicMohService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisService redisService;

    @Override
    public ApiResult updatePublicMoh(PublicMoh publicMoh, int moh) {
        if (publicMoh.getId() == null || publicMoh.getId() <= 0) {
            return new ApiResult(ApiResult.FAIL_RESULT, "公共等待语音id不正确");
        }

        if (moh == 0) {
            PublicMoh publicMoh1 = selectByPrimaryKey(publicMoh.getId());
            if (publicMoh1 == null) {
                return new ApiResult(ApiResult.FAIL_RESULT, "不存在此公共语言id");
            }

            int success = deleteByPrimaryKey(publicMoh.getId());
            if (success == 1) {
                setRefreshCache("deleteCache", publicMoh1);
                return new ApiResult(ApiResult.SUCCESS_RESULT, ApiResult.SUCCESS_DESCRIPTION);
            }
        }

        if (moh == 1) {
            int success = insertSelective(publicMoh);
            if (success == 1) {
                setRefreshCache("setCache", publicMoh);
                return new ApiResult(ApiResult.SUCCESS_RESULT, ApiResult.SUCCESS_DESCRIPTION);
            }
        }

        return new ApiResult(ApiResult.FAIL_RESULT, "更新失败");
    }

    public String getKey(PublicMoh publicMoh) {
        return String.format(CacheKey.PUBLIC_MOH_NAME, publicMoh.getName());
    }

    public void setCache(PublicMoh publicMoh) {
        redisService.set(Const.REDIS_DB_CONF_INDEX, getKey(publicMoh), publicMoh);
    }

    public void deleteCache(PublicMoh publicMoh) {
        redisService.delete(Const.REDIS_DB_CONF_INDEX, getKey(publicMoh));
    }

    private void setRefreshCache(String methodName, PublicMoh publicMoh) {
        try {
            Method method = this.getClass().getMethod(methodName, PublicMoh.class);
            AfterReturningMethod afterReturningMethod = new AfterReturningMethod(method, this, publicMoh);
            ProviderFilter.LOCAL_METHOD.set(afterReturningMethod);
        } catch (Exception e) {
            logger.error("PublicMohServiceImp.setRefreshCatche error, failed to refresh cache, class = " + this.getClass().getName(), e);
        }
    }
}
