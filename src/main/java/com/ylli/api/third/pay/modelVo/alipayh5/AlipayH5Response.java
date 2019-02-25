package com.ylli.api.third.pay.modelVo.alipayh5;

import com.google.gson.annotations.SerializedName;

public class AlipayH5Response {

    //只记录url.
    @SerializedName("pay_url")
    public String payUrl;

    //error
    public String msg;
}
