package com.tinet.ctilink.resourcemanager;


import java.io.Serializable;

/**
 * @author fengwei //
 * @date 16/4/8 17:51
 */
public class ApiResult<T> implements Serializable {
    // 成功描述
    public static final String SUCCESS_DESCRIPTION = "成功";

    public static final int SUCCESS_RESULT = 0;

    public static final int FAIL_RESULT = -1;

    private int result;  //0成功, -1失败  see ResultCode

    private String description;

    private T data;

    public ApiResult() {

    }

    public ApiResult(int result) {
        this.result = result;
        if (result == SUCCESS_RESULT) {
            this.description = SUCCESS_DESCRIPTION;
        }
    }

    //有data一定是成功的
    public ApiResult(T data) {
        this.result = SUCCESS_RESULT;
        this.description = SUCCESS_DESCRIPTION;
        this.data = data;
    }

    /**
     * 失败
     */
    public ApiResult(int result, String description) {
        this.result = result;
        this.description = description;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
        if (result == SUCCESS_RESULT) {
            this.description = SUCCESS_DESCRIPTION;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        //TODO JSON
        return super.toString();
    }

}
