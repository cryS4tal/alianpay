package com.ylli.api.wallet.model;

public class CashReq {
    public Long mchId;
    public Integer money;
    public String password;

    //格式：（xx银行）
    public String openBank;
    //格式：（安徽省芜湖市黄山路支行）
    public String subBank;
    public String bankcardNumber;
    public String name;
    //非必需
    public String identityCard;
    public String reservedPhone;
}
