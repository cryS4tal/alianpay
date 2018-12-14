package com.ylli.api.third.pay.model;

public class OrderReq {

    //商户 ID
    public String parter;

    //银行类型
    public String type;

    //金额
    public String value;

    //商户订单号
    public String orderid;

    //下行异步通知地址
    public String callbackurl;

    //下行同步通知地址
    public String hrefbackurl;

    //支付用户 IP
    public String payerIp;

    //备注消息
    public String attach;

    //MD5 签名
    public String sign;
}
