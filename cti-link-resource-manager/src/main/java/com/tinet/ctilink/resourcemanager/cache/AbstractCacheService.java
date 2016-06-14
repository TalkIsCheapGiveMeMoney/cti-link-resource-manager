package com.tinet.ctilink.resourcemanager.cache;

import com.tinet.ctilink.service.BaseService;
import org.springframework.stereotype.Component;

/**
 * @author fengwei //
 * @date 16/5/9 15:04
 */
@Component
public abstract class AbstractCacheService<T> extends BaseService<T> implements ResourceManagerCacheInterface {

}
