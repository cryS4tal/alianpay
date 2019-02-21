package com.ylli.api.third.pay.modelVo.cxt;

import com.google.gson.annotations.SerializedName;

public class CXTOrder {

    public String uid;

    public String orderid;

    public String istype;

    public String key;

    public String goodsname;

    public String price;

    public String token;

    public String format;

    @SerializedName("notify_url")
    public String notifyUrl;

    @SerializedName("return_url")
    public String returnUrl;

    public String orderuid;
}
