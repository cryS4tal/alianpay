package com.ylli.api.wallet.model;

public class CashLogDetail extends CashLog{

    //
    public String phone;

    //todo 加入用户的基本信息


    public CashLogDetail(String phone) {
        this.phone = phone;
    }

    public CashLogDetail(Long userId, Integer money, Boolean isOk, String phone) {
        super(userId, money, isOk);
        this.phone = phone;
    }
}
