package com.ylli.api.xfpay.model;

public class Wage {
    public Long userId;
    public Integer amount;
    public String accountNo;
    public String accountName;
    public String mobileNo; //not required
    public String bankNo;
    public Integer userType;
    public Integer accountType;
    public String memo;     //not required
    //商户订单号 - 对应 subNo.
    public String orderNo;
}
