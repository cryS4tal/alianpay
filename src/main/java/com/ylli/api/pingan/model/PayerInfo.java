package com.ylli.api.pingan.model;

import java.io.Serializable;

/**
 * **********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.gateway.dto
 * @Author: yangdf
 * @Date 2018/5/9 11:05
 * @Description: 付款方账户信息
 * @ModifyDetail
 * @ModifyDate
 */
public class PayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 付款方账户(对公账户号、银行卡号)
     */
    private String payerAccountNo;

    /**
     * 付款方账户名称
     **/
    private String payerAccountName;

    /**
     * 付款方账户简码(银行简码，如：ICBC、BOC、ABC等)
     **/
    private String payerBankShortCode;

    /**
     * 付款方银行名称
     */
    private String payerBankName;

    /**
     * 付款方账户类型 必选 0：借记 1：贷记 2：准贷记
     */
    private String payerBankCardType;

    /**
     * 付款方联行号
     **/
    private String payerBankBranchId;

    /**
     * 付款方支行名称
     */
    private String payerBankBranchName;

    /**
     * 付款方支行所在省
     */
    private String payerBankProvince;

    /**
     * 付款方银行编号
     */
    private String payerBankCode;
    /**
     * 付款方支行所在区域代码
     */
    private String payerBankAreaCode;

    /**
     * 支付账户限制(空不限制,LIMIT不支持信用卡)
     **/
    private String payerAccountLimit;

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
     * 快捷签约号
     */
    private String signNo;
    /**
     * 卡片有效期
     */
    private String expireDate;

    /**
     * 商户客户号
     */
    private String merCustId;

    /**
     * 商户客户注册时间
     */
    private String merCustRegistTime;

    public String getMerCustRegistTime() {
        return merCustRegistTime;
    }

    public void setMerCustRegistTime(String merCustRegistTime) {
        this.merCustRegistTime = merCustRegistTime;
    }

    public String getMerCustId() {
        return merCustId;
    }

    public void setMerCustId(String merCustId) {
        this.merCustId = merCustId;
    }

    public String getSignNo() {
        return signNo;
    }

    public void setSignNo(String signNo) {
        this.signNo = signNo;
    }

    public String getPayerBankCode() {
        return payerBankCode;
    }

    public void setPayerBankCode(String payerBankCode) {
        this.payerBankCode = payerBankCode;
    }

    public String getPayerBankAreaCode() {
        return payerBankAreaCode;
    }

    public void setPayerBankAreaCode(String payerBankAreaCode) {
        this.payerBankAreaCode = payerBankAreaCode;
    }

    public String getPayerBankName() {
        return payerBankName;
    }

    public void setPayerBankName(String payerBankName) {
        this.payerBankName = payerBankName;
    }

    public String getPayerBankBranchName() {
        return payerBankBranchName;
    }

    public void setPayerBankBranchName(String payerBankBranchName) {
        this.payerBankBranchName = payerBankBranchName;
    }

    public String getPayerBankProvince() {
        return payerBankProvince;
    }

    public void setPayerBankProvince(String payerBankProvince) {
        this.payerBankProvince = payerBankProvince;
    }

    public String getPayerAccountLimit() {
        return payerAccountLimit;
    }

    public void setPayerAccountLimit(String payerAccountLimit) {
        this.payerAccountLimit = payerAccountLimit;
    }

    public String getPayerAccountNo() {
        return payerAccountNo;
    }

    public void setPayerAccountNo(String payerAccountNo) {
        this.payerAccountNo = payerAccountNo;
    }

    public String getPayerAccountName() {
        return payerAccountName;
    }

    public void setPayerAccountName(String payerAccountName) {
        this.payerAccountName = payerAccountName;
    }

    public String getPayerBankShortCode() {
        return payerBankShortCode;
    }

    public void setPayerBankShortCode(String payerBankShortCode) {
        this.payerBankShortCode = payerBankShortCode;
    }

    public String getPayerBankCardType() {
        return payerBankCardType;
    }

    public void setPayerBankCardType(String payerBankCardType) {
        this.payerBankCardType = payerBankCardType;
    }

    public String getPayerBankBranchId() {
        return payerBankBranchId;
    }

    public void setPayerBankBranchId(String payerBankBranchId) {
        this.payerBankBranchId = payerBankBranchId;
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
        return "PayerInfo{" +
                ", payerAccountNo='" + payerAccountNo + '\'' +
                ", payerAccountName='" + payerAccountName + '\'' +
                ", payerBankShortCode='" + payerBankShortCode + '\'' +
                ", payerBankCardType='" + payerBankCardType + '\'' +
                ", payerBankBranchId='" + payerBankBranchId + '\'' +
                ", mobile='" + mobile + '\'' +
                ", certType='" + certType + '\'' +
                ", certNo='" + certNo + '\'' +
                ", cvn2='" + cvn2 + '\'' +
                ", expireDate='" + expireDate + '\'' +
                '}';
    }
}
