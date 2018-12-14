package com.ylli.api.third.pay.model;

import com.google.gson.annotations.SerializedName;

public class NotifyRes {
    @SerializedName("mch_order_id")
    public String mchOrderId;
    @SerializedName("sys_order_id")
    public String sysOrderId;
    public String money;
    @SerializedName("trade_time")
    public String tradeTime;
    public String reserve;
    public String sign;
    public String status;
}
