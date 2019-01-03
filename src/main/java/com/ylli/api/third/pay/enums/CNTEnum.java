package com.ylli.api.third.pay.enums;

public enum CNTEnum {
    BUY("1"), //下单
    CASH("0"),    //提现
    ALIPAY("0"),  //支付宝
    WX("1");  //微信

    private String value;

    CNTEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
