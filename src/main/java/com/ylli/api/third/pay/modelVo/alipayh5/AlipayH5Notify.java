package com.ylli.api.third.pay.modelVo.alipayh5;

import com.google.gson.annotations.SerializedName;

public class AlipayH5Notify {

    @SerializedName("order_no")
    public String orderNo;

    public String amount;

    @SerializedName("receipt_amount")
    public String receiptAmount;

    @SerializedName("pay_time")
    public String payTime;

    @SerializedName("trade_no")
    public String tradeNo;

    public String param;

    public String sign;
}
