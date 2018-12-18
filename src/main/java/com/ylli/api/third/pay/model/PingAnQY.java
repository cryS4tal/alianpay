package com.ylli.api.third.pay.model;

/**
 * 测试类
 */
public class PingAnQY {

    //企业签约帐号
    public String acctNo;
    //单位代码
    public String corpId;
    //银企代码
    public String yqdm;

    public PingAnQY(String acctNo, String corpId, String yqdm) {
        this.acctNo = acctNo;
        this.corpId = corpId;
        this.yqdm = yqdm;
    }
}
