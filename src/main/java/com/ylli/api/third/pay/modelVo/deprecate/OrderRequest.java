package com.ylli.api.third.pay.modelVo.deprecate;


public class OrderRequest {
    //商户ID
    public Long mchId;

    //应用ID
    public String appId;

    //支付产品ID
    public Integer productId;

    //商户订单号
    public String mchOrderNo;

    //币种
    public String currency;

    //支付金额
    public Integer amount;

    //客户端IP
    public String clientIp;

    //设备
    public String device;

    //支付结果前端跳转URL
    public String returnUrl;

    //支付结果后台回调URL
    public String notifyUrl;

    //商品主题
    public String subject;

    //商品描述信息
    public String body;

    //扩展参数1
    public String param1;

    //扩展参数2
    public String param2;

    //附加参数
    public String extra;

    //签名
    public String sign;

    public OrderRequest(Long mchId, String appId, Integer productId, String mchOrderNo, String currency, Integer amount, String clientIp, String device, String returnUrl, String notifyUrl, String subject, String body, String param1, String param2, String extra) {
        this.mchId = mchId;
        this.appId = appId;
        this.productId = productId;
        this.mchOrderNo = mchOrderNo;
        this.currency = currency;
        this.amount = amount;
        this.clientIp = clientIp;
        this.device = device;
        this.returnUrl = returnUrl;
        this.notifyUrl = notifyUrl;
        this.subject = subject;
        this.body = body;
        this.param1 = param1;
        this.param2 = param2;
        this.extra = extra;
    }

    public OrderRequest() {
    }
}
