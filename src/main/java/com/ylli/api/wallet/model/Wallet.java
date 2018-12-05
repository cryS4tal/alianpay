package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_wallet")
public class Wallet {

    @Id
    public Long id;

    //总金额   todo  账户余额
    public Integer total;

    //充值金额   todo   交易（充值）金额          计算公式：交易金额 = 订单金额 - 手续费          手续费 = 订单金额 * 费率百分比
    public Integer recharge;

    //分润金额   todo    分润金额....   分润暂时没有
    public Integer bonus;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
