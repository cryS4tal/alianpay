package com.ylli.api.user.model;

public class UserOwnInfo {

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

    //本地校验. support
    public String bankType;

    //开户行
    public String openBank;

    //开户支行
    public String subBank;

}
