package com.ylli.api.xfpay.model;

public class XfPaymentResponse {

    public String sign;

    public String message;

    //本次应答的随机密钥
    public String tm;

    public String data;

    //商户号
    public String merchantId;

    public String code;
}
