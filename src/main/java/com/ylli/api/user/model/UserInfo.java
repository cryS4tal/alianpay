package com.ylli.api.user.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_user_info")
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    /**
     * 银行卡四要素
     *
     * 姓名
     * 身份证
     * 银行卡号
     * 预留手机号
     */
    public String name;

    public String identityCard;

    public String bankcardNumber;

    public String reservedPhone;

    public String bankType;

    //开户行
    public String openBank;

    //开户支行
    public String subBank;

    /**
     * 预留字段
     * 提现类型/提现费率
     * chargeType = 1; 定额；chargeType = 2; 百分比
     * chargeRate 分 / n % 100 * 100.
     */
    public String chargeType;

    public String chargeRate;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
