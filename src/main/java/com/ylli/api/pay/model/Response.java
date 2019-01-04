package com.ylli.api.pay.model;

import com.google.common.base.Strings;

public class Response {

    public static final String URL = "url";
    public static final String FORM = "form";

    public String code;

    public String message;

    public String sign;

    //返回data类型。
    public String type;

    //具体待定义.
    public Object data;

    public Response(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Response(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Response() {
    }

    public Response(String code, String message, String sign, String type, Object data) {
        this.code = code;
        this.message = message;
        this.sign = sign;
        this.type = type;
        this.data = data;
    }

    public Response(String code, String message, String sign, Object data) {
        this.code = code;
        this.message = message;
        this.sign = sign;
        this.data = data;
    }

    /**
     * A000 - 成功。
     */
    public static Response A000(String sign, String type, Object data) {
        return new Response("A00", "成功", sign, type, data);
    }

    /**
     * A001 - 签名校验失败
     */
    public static Response A001(String message, Object data) {
        return new Response("A001", Strings.isNullOrEmpty(message) ? "签名校验失败" : message, data);
    }

    /**
     * A002 - 请先上传商户私钥
     */
    public static Response A002(String message, Object data) {
        return new Response("A002", Strings.isNullOrEmpty(message) ? "请先上传商户私钥" : message, data);
    }

    /**
     * A003 - 非法的请求参数：%s
     * 用于基本参数校验.
     */
    public static Response A003(String message, Object data) {
        return new Response("A003", new StringBuffer("非法的请求参数: ").append(message).toString(), data);
    }

    /**
     * A004 - 订单号重复
     */
    public static Response A004(String message, Object data) {
        return new Response("A004", Strings.isNullOrEmpty(message) ? "订单号重复" : message, data);
    }
}
