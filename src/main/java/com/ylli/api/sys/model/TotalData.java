package com.ylli.api.sys.model;

public class TotalData {
    //success money
    public Long smoney;

    public Integer scount;

    //fail money
    public Long fmoney;

    public Integer fcount;

    //total money
    public Long tmoney;

    //cash money
    public Long cmoney;

    //remain money = tmoney - cmoney
    public Long rmoney;
}
