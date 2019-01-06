package com.ylli.api.pay.model;

// TODO 整合至response。
public class OrderQueryRes {

    public String sysOrderId;

    public String mchOrderId;

    public Integer money;

    public String status;

    public String sign;

    public String tradeTime;

    public String code;

    public String message;

    public OrderQueryRes(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public OrderQueryRes() {
    }
}
