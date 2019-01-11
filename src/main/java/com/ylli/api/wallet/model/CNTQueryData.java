package com.ylli.api.wallet.model;

public class CNTQueryData {

    public Long id;

    //格式：（xx银行）
    public String openBank;
    //格式：（安徽省芜湖市黄山路支行）
    public String subBank;
    public String bankcardNumber;
    public String name;

    public Integer state;

    public Long mchId;
    public Integer money;
}
