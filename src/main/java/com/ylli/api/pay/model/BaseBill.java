package com.ylli.api.pay.model;

/**
 * 核心账单类.
 * todo 账单系统没有统一之前，先由其他账单转化
 */
public class BaseBill {

    //交易金额
    public Integer money;

    //手续费
    public Integer mchCharge;

    //商户号
    public Long mchId;

    //商户名
    public String mchName;

    //商户订单号
    public String mchOrderId;

    //系统订单号
    public String sysOrderId;

    //支付方式：支付宝/微信   +   tradeType  native  wap
    public String payType;

    public String state;

    public String tradeTime;

    //创建时间
    public String createTime;

}
