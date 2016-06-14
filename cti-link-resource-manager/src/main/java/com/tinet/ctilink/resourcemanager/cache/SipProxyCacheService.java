package com.tinet.ctilink.resourcemanager.cache;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.resourcemanager.model.SipProxy;
import com.tinet.ctilink.inc.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Condition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author fengwei //
 * @date 16/5/9 16:47
 */
@Component
public class SipProxyCacheService extends AbstractCacheService<SipProxy> {
    @Autowired
    private RedisService redisService;

    @Override
    public boolean reloadCache() {
        Condition condition = new Condition(SipProxy.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("active", 1);  //查询激活的
        List<SipProxy> sipProxyList = selectByCondition(condition);

        Set<String> dbKeySet = new HashSet<>();
        if (sipProxyList != null && !sipProxyList.isEmpty()) {
            redisService.set(Const.REDIS_DB_CONF_INDEX, CacheKey.SIP_PROXY, sipProxyList);

            for (SipProxy sipProxy : sipProxyList) {
                redisService.set(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SIP_PROXY_IP_ADDR
                        , sipProxy.getIpAddr()), sipProxy);
                dbKeySet.add(String.format(CacheKey.SIP_PROXY_IP_ADDR, sipProxy.getIpAddr()));
            }
        } else {
            redisService.delete(Const.REDIS_DB_CONF_INDEX, CacheKey.SIP_PROXY);
        }

        Set<String> existKeySet = redisService.scan(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SIP_PROXY_IP_ADDR
                , "*"));
        existKeySet.removeAll(dbKeySet);
        if (existKeySet.size() > 0) {
            redisService.delete(Const.REDIS_DB_CONF_INDEX, existKeySet);
        }
        return true;
    }
}
