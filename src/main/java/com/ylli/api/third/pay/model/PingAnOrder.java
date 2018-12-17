package com.ylli.api.third.pay.model;

/**
 * 平安 单笔付款 KHKF03
 */
public class PingAnOrder {

    public String orderNumber;//订单号
    public String acctNo;//企业签约帐号
    public String busiType;//费项代码
    public String corpId;//单位代码
    public String ccyCode;//币种
    public String tranAmount;//金额
    public String inAcctNo;//收款卡号
    public String inAcctName;//收款户名
    public String inAcctBankName;//收款方银行名称
    public String inAcctBankNode;//收款方联行号
    public String mobile;//收款方手机号
    public String remark;//用途/备注
    public String inAcctProvinceName;//收款方开户行省份
    public String inAcctCityName;//收款方开户行城市
}
