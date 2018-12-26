package com.ylli.api.pay.model;

import java.util.ArrayList;
import java.util.List;

public class BankPayOrder {

    //对公账户
    public static final Integer PAY_TYPE_COMPANY = 2;
    //对私
    public static final Integer PAY_TYPE_PERSON = 1;

    //借记卡
    public static final Integer DEBIT_CARD = 1;
    //贷记卡
    public static final Integer CREDIT_CARD = 2;

    public static List<Integer> payAllows = new ArrayList<Integer>(){
        {
            add(PAY_TYPE_PERSON);
            add(PAY_TYPE_COMPANY);
        }
    };

    public static List<Integer> accAllows = new ArrayList<Integer>(){
        {
            add(DEBIT_CARD);
            add(CREDIT_CARD);
        }
    };


    /**
     * 必传字段：
     * mch_id 商户号
     * mch_order_id 商户订单号
     * money 金额/分
     * accNo 收款人卡号
     * accName 收款人姓名
     */

    public Long mchId;

    public String mchOrderId;

    public Integer money;

    public String accNo;

    public String accName;

    /**
     * 默认值
     * payType 代付类型 （1.对私(默认)，2.对公）对应先锋支付userType
     * accType 账户类型 （1.借记卡（默认），2.贷记卡，payType = 1 时可选）对应 先锋支付accountType
     * issuer 联行号，payType = 2 对公转账必传。
     */
    public Integer payType;

    public Integer accType;

    public String issuer;

    /**
     * 非必填
     * mobile 手机号
     * bankName 银行名称
     * bankNo 银行编码
     */
    public String mobile;

    public String bankName;

    public String bankNo;

    public String noyifyUrl;


    public String sign;
}
