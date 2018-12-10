package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_cash_log")
public class CashLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long mchId;
    public Integer money;

    //格式：（xx银行）
    public String openBank;
    //格式：（安徽省芜湖市黄山路支行）
    public String subBank;
    public String bankcardNumber;
    public String name;
    //非必需
    public String identityCard;
    public String reservedPhone;

    public Boolean isOk;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
