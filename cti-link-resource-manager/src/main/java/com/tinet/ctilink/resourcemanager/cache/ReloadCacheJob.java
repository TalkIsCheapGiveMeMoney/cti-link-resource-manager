package com.tinet.ctilink.resourcemanager.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * @author fengwei //
 * @date 16/5/10 10:21
 */
public class ReloadCacheJob implements Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private List<ResourceManagerCacheInterface> resourceManagerCacheInterfaceList;

    @Override
    public void run() {
        Date start = new Date();
        logger.info("reload start , startTime:" + start.getTime()/1000);
        if (resourceManagerCacheInterfaceList != null) {
            for (ResourceManagerCacheInterface cacheInterface : resourceManagerCacheInterfaceList) {
                cacheInterface.reloadCache();
            }
        }
        Date end = new Date();
        logger.info("reload end , endTime:" + end.getTime()/1000 + ", 耗时" + (end.getTime()/1000 - start.getTime()/1000) + "秒");
    }

    public List<ResourceManagerCacheInterface> getResourceManagerCacheInterfaceList() {
        return resourceManagerCacheInterfaceList;
    }

    public void setResourceManagerCacheInterfaceList(List<ResourceManagerCacheInterface> resourceManagerCacheInterfaceList) {
        this.resourceManagerCacheInterfaceList = resourceManagerCacheInterfaceList;
    }
}
