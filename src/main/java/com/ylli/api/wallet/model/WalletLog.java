package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_wallet_log")
public class WalletLog {

    public static final String CZ = "充值";
    public static final String TX = "提现";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long adminId;

    public Long userId;

    public String type;

    public Integer money;

    public Integer currentMoney;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
