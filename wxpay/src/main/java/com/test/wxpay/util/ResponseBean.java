package com.test.wxpay.util;

public class ResponseBean {

    private Integer code;
    private String message;
    private Object data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseBean{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

    public static ResponseBean getSuccess(Object data) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(200);
        responseBean.setMessage("success");
        responseBean.setData(data);
        return responseBean;
    }

    public static ResponseBean getFailure(String message) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(500);
        responseBean.setMessage(message);
        return responseBean;
    }

    public static ResponseBean getFailure(String message, Object data) {
        ResponseBean responseBean = new ResponseBean();
        responseBean.setCode(500);
        responseBean.setMessage(message);
        responseBean.setData(data);
        return responseBean;
    }

}
