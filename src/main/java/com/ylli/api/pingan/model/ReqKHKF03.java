package com.ylli.api.pingan.model;

import java.io.Serializable;

/**
 * 3.3 单笔付款申请
 */
public class ReqKHKF03 implements Serializable {
    private static final long serialVersionUID = -2676315561770888175L;

    private String OrderNumber;//订单号
    private String AcctNo;//企业签约帐号
    private String BusiType;//费项代码
    private String CorpId;//单位代码
    private String CcyCode;//币种
    private String TranAmount;//金额
    private String InAcctNo;//收款卡号
    private String InAcctName;//收款户名
    private String InAcctBankName;//收款方银行名称
    private String InAcctBankNode;//收款方联行号
    private String Mobile;//收款方手机号
    private String Remark;//用途/备注
    private String InAcctProvinceName;//收款方开户行省份
    private String InAcctCityName;//收款方开户行城市


    public String getOrderNumber() {
        return OrderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        OrderNumber = orderNumber;
    }

    public String getAcctNo() {
        return AcctNo;
    }

    public void setAcctNo(String acctNo) {
        AcctNo = acctNo;
    }

    public String getBusiType() {
        return BusiType;
    }

    public void setBusiType(String busiType) {
        BusiType = busiType;
    }

    public String getCorpId() {
        return CorpId;
    }

    public void setCorpId(String corpId) {
        CorpId = corpId;
    }

    public String getCcyCode() {
        return CcyCode;
    }

    public void setCcyCode(String ccyCode) {
        CcyCode = ccyCode;
    }

    public String getTranAmount() {
        return TranAmount;
    }

    public void setTranAmount(String tranAmount) {
        TranAmount = tranAmount;
    }

    public String getInAcctNo() {
        return InAcctNo;
    }

    public void setInAcctNo(String inAcctNo) {
        InAcctNo = inAcctNo;
    }

    public String getInAcctName() {
        return InAcctName;
    }

    public void setInAcctName(String inAcctName) {
        InAcctName = inAcctName;
    }

    public String getInAcctBankName() {
        return InAcctBankName;
    }

    public void setInAcctBankName(String inAcctBankName) {
        InAcctBankName = inAcctBankName;
    }

    public String getInAcctBankNode() {
        return InAcctBankNode;
    }

    public void setInAcctBankNode(String inAcctBankNode) {
        InAcctBankNode = inAcctBankNode;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getInAcctProvinceName() {
        return InAcctProvinceName;
    }

    public void setInAcctProvinceName(String inAcctProvinceName) {
        InAcctProvinceName = inAcctProvinceName;
    }

    public String getInAcctCityName() {
        return InAcctCityName;
    }

    public void setInAcctCityName(String inAcctCityName) {
        InAcctCityName = inAcctCityName;
    }
}
