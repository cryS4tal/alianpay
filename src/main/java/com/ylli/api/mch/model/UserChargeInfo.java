package com.ylli.api.mch.model;

public class UserChargeInfo {

    public Long userId;

    /**
     * 预留字段
     * 提现类型/提现费率
     * chargeType = 1; 定额；chargeType = 2; 百分比
     * chargeRate 分 / n % 100 * 100.
     */
    public Integer chargeType;

    public Integer chargeRate;


    public static final Integer TYPE_FIX = 1;
    public static final Integer TYPE_FLOAT = 2;

}
