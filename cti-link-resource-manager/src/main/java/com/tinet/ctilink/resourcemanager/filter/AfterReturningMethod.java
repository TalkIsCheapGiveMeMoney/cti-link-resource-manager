package com.tinet.ctilink.resourcemanager.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author fengwei //
 * @date 16/4/15 09:43
 *
 * after returning method
 */
public class AfterReturningMethod {

    private Method method;

    private Object obj;

    private Object[] args;

    public AfterReturningMethod(Method method, Object obj, Object... args) {
        this.method = method;
        this.obj = obj;
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }


    public Object invoke() throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        System.out.println("AfterReturningMethod invoke, ClassName:" + obj.getClass().getName() + ", MethodName:" + method.getName());
        return method.invoke(obj, args);
    }
}
