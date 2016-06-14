package com.tinet.ctilink.resourcemanager.filter;

import com.alibaba.dubbo.rpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fengwei //
 * @date 16/4/9 12:02
 */
public class ProviderFilter implements Filter {

    private final static Logger logger = LoggerFactory.getLogger(ProviderFilter.class);

    //一个请求使用一个线程, 缓存刷新方法放到ThreadLocal里面
    public static ThreadLocal<AfterReturningMethod> LOCAL_METHOD = new ThreadLocal<>();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        //IP控制 + 限流

        Result result = null;
        String m = "";
        try {
            result = invoker.invoke(invocation);
            try {
                AfterReturningMethod method = LOCAL_METHOD.get();
                if (method != null) {
                    m = "ClassName:" + method.getObj().getClass().getName() + ", MethodName:" + method.getMethod().getName();
                    method.invoke();
                }
            } catch (Exception e) {
                logger.error("AfterReturningMethod invoke error, ", e);
            }

        } finally {
            //线程可能重用, 每次调用必须remove
            if (!m.equals("")) {
                System.out.println("AfterReturningMethod remove," + m);
            }
            //待改进, 每个接口都会执行
            LOCAL_METHOD.remove();
        }

        return result;
    }
}
