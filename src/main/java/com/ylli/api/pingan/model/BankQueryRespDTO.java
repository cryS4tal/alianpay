package com.ylli.api.pingan.model;

/**
 * **********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.gateway.dto
 * @Author: yangdf
 * @Date 2018/5/8 17:00
 * @Description: 查询响应结果
 * @ModifyDetail
 * @ModifyDate
 */
public class BankQueryRespDTO extends BaseGatewayRespDTO {
    private static final long serialVersionUID = 3928374298825466119L;

    /**
     * 原渠道交易流水号
     */
    private String orgChannelSerialNo;

    /**
     * 渠道返回的查询流水号
     */
    private String outSerialNo;

    /**
     * 外部渠道交易清算日期YYYYMMDD<br>
     * 渠道返回的是MMDD，需补上YYYY
     */
    private String outSettleDate;

    /**
     * 交易状态：各前置已将外部渠道状态进行转换，对应TxChannelStatusEnum
     */
    private String txStatus;

    public String getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(String txStatus) {
        this.txStatus = txStatus;
    }

    public String getOrgChannelSerialNo() {
        return orgChannelSerialNo;
    }

    public void setOrgChannelSerialNo(String orgChannelSerialNo) {
        this.orgChannelSerialNo = orgChannelSerialNo;
    }

    public String getOutSerialNo() {
        return outSerialNo;
    }

    public void setOutSerialNo(String outSerialNo) {
        this.outSerialNo = outSerialNo;
    }

    public String getOutSettleDate() {
        return outSettleDate;
    }

    public void setOutSettleDate(String outSettleDate) {
        this.outSettleDate = outSettleDate;
    }

    @Override
    public String toString() {
        return super.toString() + "BankQueryRespDTO [orgChannelSerialNo=" + orgChannelSerialNo + ", outSerialNo="
                + outSerialNo + ", outSettleDate=" + outSettleDate + "]";
    }

}
