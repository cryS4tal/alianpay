package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_wallet")
public class Wallet {

    @Id
    public Long id;

    //账户余额
    public Integer total;

    //交易（充值）金额      计算公式：交易金额 = 订单金额 - 手续费      手续费 = 订单金额 * 费率百分比
    public Integer recharge;

    //待处理金额（提现申请）
    public Integer pending;

    //分润金额
    public Integer bonus;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
