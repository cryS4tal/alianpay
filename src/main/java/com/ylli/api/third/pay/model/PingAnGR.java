package com.ylli.api.third.pay.model;

public class PingAnGR {

    public String inAcctNo;
    //收款户名
    public String inAcctName;
    //收款方银行名称
    public String inAcctBankName;
    //收款方手机号
    public String mobile;

    public PingAnGR(String inAcctNo, String inAcctName, String inAcctBankName, String mobile) {
        this.inAcctNo = inAcctNo;
        this.inAcctName = inAcctName;
        this.inAcctBankName = inAcctBankName;
        this.mobile = mobile;
    }
}
