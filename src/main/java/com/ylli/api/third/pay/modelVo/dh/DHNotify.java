package com.ylli.api.third.pay.modelVo.dh;

import com.google.gson.annotations.SerializedName;

public class DHNotify {

    public String memberid;

    public String orderid;

    public String amount;

    @SerializedName("transaction_id")
    public String transactionid;

    public String datetime;

    public String returncode;

    public String attach;

    public String sign;
}
