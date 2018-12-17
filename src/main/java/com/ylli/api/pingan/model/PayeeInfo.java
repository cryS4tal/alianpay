package com.ylli.api.pingan.model;

import java.io.Serializable;

/**
 * Function: 收款方信息VO <br/>
 * Date: 2017年2月28日 上午11:37:43 <br/>
 */
public class PayeeInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 收款方名称
     */
    private String payeeName;

    /**
     * 收款方账户(对公账户号、银行卡号)
     */
    private String payeeAccountNo;

    /**
     * 收款方账户名称
     **/
    private String payeeAccountName;
    /**
     * 收款方账户简码(银行简码，如：ICBC、BOC、ABC等)
     **/
    private String payeeBankShortCode;

    /**
     * 收款方账户类型 必选 0：借记 1：贷记 2：准贷记
     */
    private String payeeBankCardType;
    /**
     * 收款方银行名称
     */
    private String payeeBankName;
    /**
     * 收款方联行号
     **/
    private String payeeBankBranchId;
    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 证件类型
     */
    private String certType;

    /**
     * 证件号码
     */
    private String certNo;
    /**
     * cvn2
     */
    private String cvn2;

    /**
     * 卡片有效期
     */
    private String expireDate;

    public String getPayeeBankName() {
        return payeeBankName;
    }

    public void setPayeeBankName(String payeeBankName) {
        this.payeeBankName = payeeBankName;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    public String getPayeeAccountNo() {
        return payeeAccountNo;
    }

    public void setPayeeAccountNo(String payeeAccountNo) {
        this.payeeAccountNo = payeeAccountNo;
    }

    public String getPayeeAccountName() {
        return payeeAccountName;
    }

    public void setPayeeAccountName(String payeeAccountName) {
        this.payeeAccountName = payeeAccountName;
    }

    public String getPayeeBankShortCode() {
        return payeeBankShortCode;
    }

    public void setPayeeBankShortCode(String payeeBankShortCode) {
        this.payeeBankShortCode = payeeBankShortCode;
    }

    public String getPayeeBankCardType() {
        return payeeBankCardType;
    }

    public void setPayeeBankCardType(String payeeBankCardType) {
        this.payeeBankCardType = payeeBankCardType;
    }

    public String getPayeeBankBranchId() {
        return payeeBankBranchId;
    }

    public void setPayeeBankBranchId(String payeeBankBranchId) {
        this.payeeBankBranchId = payeeBankBranchId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCertType() {
        return certType;
    }

    public void setCertType(String certType) {
        this.certType = certType;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getCvn2() {
        return cvn2;
    }

    public void setCvn2(String cvn2) {
        this.cvn2 = cvn2;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    @Override
    public String toString() {
        return "PayeeInfo{" +
                "payeeName='" + payeeName + '\'' +
                ", payeeAccountNo='" + payeeAccountNo + '\'' +
                ", payeeAccountName='" + payeeAccountName + '\'' +
                ", payeeBankShortCode='" + payeeBankShortCode + '\'' +
                ", payeeBankCardType='" + payeeBankCardType + '\'' +
                ", payeeBankBranchId='" + payeeBankBranchId + '\'' +
                ", mobile='" + mobile + '\'' +
                ", certType='" + certType + '\'' +
                ", certNo='" + certNo + '\'' +
                ", cvn2='" + cvn2 + '\'' +
                ", expireDate='" + expireDate + '\'' +
                '}';
    }
}
