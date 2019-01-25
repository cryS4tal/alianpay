package com.ylli.api.pay.model;

import java.sql.Timestamp;

/**
 * 核心账单类.
 */
public class BaseBill {

    //下单金额/分
    public Integer money;

    //实际成交金额/元
    public String msg;

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

    //上游订单号
    public String superOrderId;

    //支付方式：支付宝/微信   +   tradeType  native  wap
    public String payType;

    public Integer state;

    public Timestamp tradeTime;

    public String channel;

    public Boolean isSuccess;

    //创建时间
    public Timestamp createTime;

}
