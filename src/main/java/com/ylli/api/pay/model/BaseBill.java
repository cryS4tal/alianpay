package com.ylli.api.pay.model;

import java.sql.Timestamp;

/**
 * 核心账单类.
 * todo 账单系统没有统一之前，先由其他账单转化
 */
public class BaseBill {

    public Integer money;

    public Long mchId;

    public String mchOrderId;

    public String sysOrderId;

    //支付方式：支付宝/微信
    public String payType;

    public String state;

    public String tradeTime;

}
