package com.ylli.api.pay.model;

import com.google.common.base.Strings;

public class ResponseEnum {

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

    /**
     * A005 - 金额限制：%s
     */
    public static Response A005(String message, Object data) {
        return new Response("A005", new StringBuffer("金额限制：").append(message).toString(), data);
    }

    /**
     * A006 - 订单不存在
     */
    public static Response A006(String message, Object data) {
        return new Response("A006", Strings.isNullOrEmpty(message) ? "订单不存在" : message, data);
    }

    /**
     * A099 - 下单失败：%s
     */
    public static Response A099(String message, Object data) {
        return new Response("A099", new StringBuffer("下单失败：").append(message).toString(), data);
    }

    /**
     * A100 - 商户被冻结，请联系管理员
     */
    public static Response A100(String message, Object data) {
        return new Response("A100", Strings.isNullOrEmpty(message) ? "商户被冻结，请联系管理员" : message, data);
    }

    /**
     * A999 - 系统维护
     */
    public static Response A999(String message, Object data) {
        return new Response("A999", Strings.isNullOrEmpty(message) ? "系统维护" : message, data);
    }
}
