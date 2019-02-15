package com.ylli.api.third.pay.modelVo.deprecate;

public class OrderQueryRequest {
    //商户ID
    public String mchId;
    //应用ID
    public String appId;
    //支付订单号
    public String payOrderId;
    //商户订单号
    public String mchOrderNo;
    //是否执行回调，如果为true，则支付中心会再次向商户发起一次回调，如果为false则不会发起
    public Boolean executeNotify;
    //签名
    public String sign;

    /**
     * 支付回调 executeNotify 默认true
     */
    public OrderQueryRequest(String mchId, String appId, String payOrderId, String mchOrderNo) {
        this.mchId = mchId;
        this.appId = appId;
        this.payOrderId = payOrderId;
        this.mchOrderNo = mchOrderNo;
        this.executeNotify = true;
    }

    public OrderQueryRequest() {
    }
}
