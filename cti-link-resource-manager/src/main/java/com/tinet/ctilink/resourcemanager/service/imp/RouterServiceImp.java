package com.tinet.ctilink.resourcemanager.service.imp;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.ApiResult;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.resourcemanager.filter.AfterReturningMethod;
import com.tinet.ctilink.resourcemanager.filter.ProviderFilter;
import com.tinet.ctilink.resourcemanager.mapper.GatewayMapper;
import com.tinet.ctilink.resourcemanager.mapper.RoutersetMapper;
import com.tinet.ctilink.conf.model.Gateway;
import com.tinet.ctilink.conf.model.Router;
import com.tinet.ctilink.conf.model.Routerset;
import com.tinet.ctilink.resourcemanager.response.RouterResponse;
import com.tinet.ctilink.resourcemanager.service.v1.CtiLinkRouterService;
import com.tinet.ctilink.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Condition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nope-J on 2016/6/1.
 */

@Service
public class RouterServiceImp extends BaseService<Router> implements CtiLinkRouterService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisService redisService;

    @Autowired
    private RoutersetMapper routersetMapper;

    @Autowired
    private GatewayMapper gatewayMapper;

    @Override
    public ApiResult<List<RouterResponse>> listRouter(Router router) {
        if (router.getRoutersetId() == null || router.getRoutersetId() < 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "路由组id不正确");
        }
        Routerset routerset = routersetMapper.selectByPrimaryKey(router.getRoutersetId());
        if (routerset == null) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "不存在此路由组");
        }

        Condition condition = new Condition(Router.class);
        Condition.Criteria criteria = condition.createCriteria();
        criteria.andEqualTo("routersetId", router.getRoutersetId());

        List<Router> list = selectByCondition(condition);
        if (list == null || list.size() <= 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "获取列表失败");
        }

        List<RouterResponse> routerResponseList = new ArrayList<RouterResponse>();
        for (Router router1 : list) {
            RouterResponse routerResponse = new RouterResponse();
            Gateway gateway = gatewayMapper.selectByPrimaryKey(router1.getGatewayId());
            routerResponse.setGateway(gateway);
            routerResponse.setRouterset(routerset);
            routerResponse.setId(router1.getId());
            routerResponse.setPrefix(router1.getPrefix());
            routerResponse.setPriority(router1.getPriority());
            routerResponse.setDescription(router1.getDescription());
            routerResponse.setCreateTime(router1.getCreateTime());
            routerResponseList.add(routerResponse);
        }

        return new ApiResult<>(routerResponseList);
    }

    @Override
    public ApiResult<RouterResponse> createRouter(Router router) {
        RouterResponse routerResponse = new RouterResponse();

        if (StringUtils.isEmpty(router.getPrefix())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "前缀不能为空");
        }
        routerResponse.setPrefix(router.getPrefix());

        if (StringUtils.isEmpty(router.getDescription())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "说明不能为空");
        }
        routerResponse.setDescription(router.getDescription());

        if (router.getPriority() == null || router.getPriority() < 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "优先级不正确");
        }
        routerResponse.setPriority(router.getPriority());

        Routerset routerset = routersetMapper.selectByPrimaryKey(router.getRoutersetId());
        if (routerset == null) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "不存在路由组id");
        }
        routerResponse.setRouterset(routerset);

        Gateway gateway = gatewayMapper.selectByPrimaryKey(router.getGatewayId());
        if (gateway == null) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "不存在网关id");
        }
        routerResponse.setGateway(gateway);

        router.setCreateTime(new Date());
        routerResponse.setCreateTime(router.getCreateTime());

        int success = insertSelective(router);
        if (success == 1) {
            setRefreshCacheMethod("setCache", router);
            return new ApiResult<>(routerResponse);
        }

        return new ApiResult<>(ApiResult.FAIL_RESULT, "新增失败");
    }

    @Override
    public ApiResult<RouterResponse> updateRouter(Router router) {
        RouterResponse routerResponse = new RouterResponse();

        if (router.getId() == null || router.getId() < 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "路由id不正确");
        }
        routerResponse.setId(router.getId());

        Router router1 = selectByPrimaryKey(router.getId());
        if (router1 == null) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "不存在路由id");
        }

        if (StringUtils.isEmpty(router.getPrefix())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "前缀不能为空");
        }
        routerResponse.setPrefix(router.getPrefix());

        if (StringUtils.isEmpty(router.getDescription())) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "说明不能为空");
        }
        routerResponse.setDescription(router.getDescription());

        if (router.getPriority() == null || router.getPriority() <= 0) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "优先级不正确");
        }
        routerResponse.setPriority(router.getPriority());

        Gateway gateway = gatewayMapper.selectByPrimaryKey(router.getGatewayId());
        if (gateway == null) {
            return new ApiResult<>(ApiResult.FAIL_RESULT, "不存在网关id");
        }
        routerResponse.setGateway(gateway);

        router.setCreateTime(router1.getCreateTime());
        routerResponse.setCreateTime(router1.getCreateTime());

        int success = updateByPrimaryKey(router);
        if (success == 1) {
            setRefreshCacheMethod("updateCache", router);
            return new ApiResult<>(routerResponse);
        }

        return new ApiResult<>(ApiResult.FAIL_RESULT, "更新失败");
    }

    @Override
    public ApiResult deleteRouter(Router router) {
        if (router.getId() == null || router.getId() < 0) {
            return new ApiResult(ApiResult.FAIL_RESULT, "路由id不正确");
        }

        Router router1 = selectByPrimaryKey(router.getId());
        if (router1 == null) {
            return new ApiResult(ApiResult.FAIL_RESULT, "不存在此路由id");
        }

        int success = deleteByPrimaryKey(router.getId());
        if (success == 1) {
            setRefreshCacheMethod("deleteCache", router1);
            return new ApiResult(ApiResult.SUCCESS_RESULT, ApiResult.SUCCESS_DESCRIPTION);
        }

        logger.error("RouterserviceImp.deleteRouter error, router = " + router + "success = " + success);
        return new ApiResult(ApiResult.FAIL_RESULT, "删除失败");
    }

    public String getKey(Router router) {
        return String.format(CacheKey.ROUTER_ROUTERSET_ID, router.getRoutersetId());
    }

    public void setCache(Router router) {
        List<Router> list = redisService.getList(Const.REDIS_DB_CONF_INDEX, getKey(router), Router.class);
        if (list == null) {
            list = new ArrayList<Router>();
        }
        list.add(router);
        redisService.set(Const.REDIS_DB_CONF_INDEX, getKey(router), list);
    }

    public void updateCache(Router router) {
        List<Router> list = redisService.getList(Const.REDIS_DB_CONF_INDEX, getKey(router), Router.class);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId().equals(router.getId())) {
                    list.set(i, router);
                    break;
                }
            }
        }
        redisService.set(Const.REDIS_DB_CONF_INDEX, getKey(router), list);
    }

    public void deleteCache(Router router) {
        List<Router> list = redisService.getList(Const.REDIS_DB_CONF_INDEX, getKey(router), Router.class);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (router.getId().equals(list.get(i).getId())) {
                    list.remove(i);
                    break;
                }
            }
        }
        redisService.set(Const.REDIS_DB_CONF_INDEX, getKey(router), list);
    }

    private void setRefreshCacheMethod(String methodName, Router router) {
        try {
            Method method = this.getClass().getMethod(methodName, Router.class);
            AfterReturningMethod afterReturningMethod = new AfterReturningMethod(method, this, router);
            ProviderFilter.LOCAL_METHOD.set(afterReturningMethod);
        } catch (Exception e) {
            logger.error("RouterServiceImp.setRefreshCache error, fail to refresh cache, class = " + this.getClass().getName());
        }
    }
}
