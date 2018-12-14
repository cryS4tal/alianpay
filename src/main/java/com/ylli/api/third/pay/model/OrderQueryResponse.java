package com.ylli.api.third.pay.model;

public class OrderQueryResponse {
    //返回状态码
    public String retCode;
    //返回信息
    public String retMsg;
    //商户ID
    public Long mchId;
    //应用ID
    public String appId;
    //支付产品ID
    public Integer productId;
    //支付订单号
    public String payOrderId;
    //商户订单号
    public String mchOrderNo;
    //支付金额
    public Integer amount;
    //币种
    public String currency;
    //状态:0-订单生成,1-支付中,2-支付成功,3-业务处理完成
    public Integer status;
    //渠道用户ID
    public String channelUser;
    //渠道订单号
    public String channelOrderNo;
    //渠道数据包
    public String channelAttach;
    //支付成功时间
    public Long paySuccTime;
}
