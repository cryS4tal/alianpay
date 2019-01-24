package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_wallet_log")
public class WalletLog {

    //线下充值
    public static final Integer XTCZ = 1;
    //余额转换
    public static final Integer YEZH = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //操作人员id
    public Long authId;

    public String authName;

    public Long mchId;

    public String mchName;

    public Integer type;

    public Integer money;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
