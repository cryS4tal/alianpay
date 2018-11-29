package com.ylli.api.pay.model;

/**
 * 用于己方系统商户下单基础类
 */
public class BaseOrder {

    public Long mchId;        //商户id  →  accountId】

    public Integer money;        //金额分

    public String mchOrderId;   //商户系统订单id

    public String notifyUrl;    //异步通知地址

    public String redirectUrl;  //前端跳转地址

    public String reserve;      //保留域.

    public String sign;         //

    //todo 加入支付类型，商户可控制支付方式，由此带来的参数差异？？
    /**
     * alipay
     * wx
     */
    public String payType;

    //待定义，解决不同渠道商参数兼容问题。
    public Object extra;

}
