package com.ylli.api.third.pay.model;

public class OrderNotifyRes {
    public String code;
    public String message;

    public OrderNotifyRes(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public OrderNotifyRes() {
    }
}
