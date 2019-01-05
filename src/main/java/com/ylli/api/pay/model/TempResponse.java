package com.ylli.api.pay.model;

import com.google.common.base.Strings;

public class TempResponse {

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
