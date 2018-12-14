package com.ylli.api.third.pay.model;

/**
 * 提供给下游服务商的单笔代发参数定义
 */
public class CreditPay extends Wage{
    //商户号
    public String merchantNo;
    //签名
    public String sign;
}
