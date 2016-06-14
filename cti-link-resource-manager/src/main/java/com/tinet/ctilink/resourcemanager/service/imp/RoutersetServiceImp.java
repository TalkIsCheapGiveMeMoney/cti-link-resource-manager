package com.tinet.ctilink.resourcemanager.service.imp;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.ApiResult;
import com.tinet.ctilink.resourcemanager.filter.AfterReturningMethod;
import com.tinet.ctilink.resourcemanager.filter.ProviderFilter;
import com.tinet.ctilink.resourcemanager.mapper.RouterMapper;
import com.tinet.ctilink.resourcemanager.model.Router;
import com.tinet.ctilink.resourcemanager.model.Routerset;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkRoutersetService;
import com.tinet.ctilink.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Condition;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by nope-J on 2016/6/1.
 */

@Service
public class RoutersetServiceImp extends BaseService<Routerset> implements CtiLinkRoutersetService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisService redisService;

    @Autowired
    private RouterMapper routerMapper;

    @Override
    public ApiResult<List<Routerset>> listRouterSet() {
        List<Routerset> list = selectAll();
        if(list != null || list.size() > 0){
            return new ApiResult<>(list);
        }

        return new ApiResult<>(ApiResult.FAIL_RESULT,"获取列表失败");
    }

    @Override
    public ApiResult<Routerset> createRouterset(Routerset routerset) {
        if(StringUtils.isEmpty(routerset.getName())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"路由组名称不能为空");
        }

        int success = insertSelective(routerset);
        if(success == 1){
            return new ApiResult<>(routerset);
        }

        return new ApiResult<>(ApiResult.FAIL_RESULT,"新增失败");
    }

    @Override
    public ApiResult<Routerset> updateRouterset(Routerset routerset) {
        if(routerset.getId() == null || routerset.getId() <= 0){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"路由组id不能为空");
        }

        Routerset routerset1 = selectByPrimaryKey(routerset.getId());
        if(routerset1 == null){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"不存在此id:"+routerset.getId());
        }

        if(StringUtils.isEmpty(routerset.getName())){
            return new ApiResult<>(ApiResult.FAIL_RESULT,"路由组名称不能为空");
        }

        routerset.setCreateTime(routerset1.getCreateTime());

        int success = updateByPrimaryKey(routerset);
        if(success == 1){
            return new ApiResult<>(routerset);
        }

        return new ApiResult<>(ApiResult.FAIL_RESULT,"更新失败");
    }

    @Override
    public ApiResult deleteRouterset(Routerset routerset) {
        Condition condition = new Condition(Router.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("routersetId",routerset.getId());
        condition.setTableName("cti_link_router");
        routerMapper.deleteByCondition(condition);

        int success = deleteByPrimaryKey(routerset.getId());
        if(success == 1){
            setRefreshCacheMethod("deleteCache",routerset);
            return new ApiResult(ApiResult.SUCCESS_RESULT,ApiResult.SUCCESS_DESCRIPTION);
        }

        logger.error("RoutersetServiceImp.deleteRouterset error, routerset = " + routerset + "success = " + success);
        return new ApiResult(ApiResult.SUCCESS_RESULT,"删除失败");
    }

    public String getKey(Routerset routerset){
        return String.format(CacheKey.ROUTER_ROUTERSET_ID,routerset.getId());
    }

    public void deleteCache(Routerset routerset){
        redisService.delete(Const.REDIS_DB_CONF_INDEX,getKey(routerset));
    }

    private void setRefreshCacheMethod(String methodName, Routerset routerset){
        try{
            Method method = this.getClass().getMethod(methodName, Routerset.class);
            AfterReturningMethod afterReturningMethod = new AfterReturningMethod(method, this, routerset);
            ProviderFilter.LOCAL_METHOD.set(afterReturningMethod);
        }catch(Exception e){
            logger.error("RouterServiceImp setRefreshCacheMethod error, fail to refresh cache, class = "+this.getClass().getName());
        }
    }
}
