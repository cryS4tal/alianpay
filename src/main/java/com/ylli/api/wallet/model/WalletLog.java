package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_wallet_log")
public class WalletLog {

    //线下充值
    public static final Integer XXCZ = 1;
    //余额转换
    public static final Integer YEZH = 2;

    //状态。
    public static final Integer ING = 1;
    public static final Integer FAIL = 2;
    public static final Integer FINISH = 3;

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

    //状态。
    public Integer status;

    //商户线下录入时，记录相关信息
    public String accountName;
    public String accountNo;
    public String recevieBank;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
