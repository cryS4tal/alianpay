package com.ylli.api.xfpay.model;

public class Data {

    //商户号
    public String merchantId;

    //商户订单号
    public String merchantNo;

    //金额
    public String amount;

    //币种
    public String transCur;

    //先锋支付平台交易订单号
    public String tradeNo;

    //交易成功时间
    public String tradeTime;

    //处理状态
    public String status;

    //保留域
    public String memo;

    //应答码
    public String resCode;

    //应答信息
    public String resMessage;
}
