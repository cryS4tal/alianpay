package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    //总金额
    public Integer totalMoney;

    //可使用金额
    public Integer avaliableMoney;

    //待确定金额
    public Integer abnormalMoney;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
