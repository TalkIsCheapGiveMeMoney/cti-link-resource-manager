package com.tinet.ctilink.resourcemanager.cache;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.model.SipGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fengwei //
 * @date 16/5/9 15:04
 */
@Component
public class SipGroupCacheService extends AbstractCacheService<SipGroup> {
    @Autowired
    private RedisService redisService;

    @Override
    public boolean reloadCache() {
        List<SipGroup> sipGroupList = selectAll();
        redisService.set(Const.REDIS_DB_CONF_INDEX, CacheKey.SIP_GROUP, sipGroupList);
        return true;
    }

}
