package com.ylli.api.pay.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "t_bank_pay_order")
public class BankPayOrder {

    public static final Integer NEW = 1;
    public static final Integer ING = 2;
    public static final Integer FINISH = 3;
    public static final Integer FAIL = 4;


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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

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

    public String sysOrderId;

    public String superOrderId;

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

    public String notifyUrl;

    @Transient
    public String sign;

    //预留 - 代付通道id
    public Long bankPaymentId;

    //预留 - 结算类型：定额 / 百分比
    public Integer chargeType;

    //预留 - 结算金额
    public Integer chargeMoney;

    //子系统是否接受通知.
    public Boolean isSuccess;

    public Integer status;

    //交易成立时间
    public Timestamp tradeTime;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
