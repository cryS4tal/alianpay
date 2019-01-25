package com.ylli.api.pay.model;

/**
 * 用于己方系统商户下单基础类
 * <p>
 * 签名函数：导致不能再此类中加入静态变量（会参与签名）
 */
public class BaseOrder {

    public Long mchId;        //商户id  →  accountId】

    public Integer money;        //金额分

    public String mchOrderId;   //商户系统订单id

    public String notifyUrl;    //异步通知地址

    public String redirectUrl;  //前端跳转地址

    public String reserve;      //保留域.

    public String sign;         //

    /**
     * alipay
     * wx
     */
    public String payType;

    public String tradeType;

    //待定义，解决不同渠道商参数兼容问题。
    public Object extra;

    /**
     * 新增加入版本号：原始第三方支付可以不传.
     * 新通道CNT version = 1.1
     */
    //public String version;

}
