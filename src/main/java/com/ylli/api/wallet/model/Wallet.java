package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_wallet")
public class Wallet {

    @Id
    public Long id;

    //总金额
    public Integer total;

    //充值金额
    public Integer recharge;

    //分润金额
    public Double bonus;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
