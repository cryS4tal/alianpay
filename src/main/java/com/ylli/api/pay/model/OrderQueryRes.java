package com.ylli.api.pay.model;

public class OrderQueryRes extends Response {

    public String sysOrderId;

    public String mchOrderId;

    public Integer money;

    public String status;

    public String sign;

    public String tradeTime;

    public OrderQueryRes(String code, String message) {
        super(code, message);
    }

    public OrderQueryRes() {
    }
}
