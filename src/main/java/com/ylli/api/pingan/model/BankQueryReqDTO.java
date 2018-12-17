package com.ylli.api.pingan.model;

import java.io.Serializable;
import java.util.Date;

/**
 * **********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.gateway.dto
 * @Author: yangdf
 * @Date 2018/5/8 16:59
 * @Description: 查询请求参数
 * @ModifyDetail
 * @ModifyDate
 */
public class BankQueryReqDTO extends BaseGatewayReqDTO implements Serializable {

    private static final long serialVersionUID = -605843757921852810L;
    private PayeeInfo payeeInfo;
    private PayerInfo payerInfo;
    /**
     * 支付、退款时上送的订单号
     **/
    private String oriOrderNo;

    /**
     * 外部渠道返回的交易流水号
     */
    private String outOrderNo;

    /**
     * 请求发起来源IP
     */
    private String orderCreateIp;

    /**
     * 发送渠道时间
     **/
    private Date sendChannelTime;

    public Date getSendChannelTime() {
        return sendChannelTime;
    }

    public void setSendChannelTime(Date sendChannelTime) {
        this.sendChannelTime = sendChannelTime;
    }

    public String getOrderCreateIp() {
        return orderCreateIp;
    }

    public void setOrderCreateIp(String orderCreateIp) {
        this.orderCreateIp = orderCreateIp;
    }

    public PayeeInfo getPayeeInfo() {
        return payeeInfo;
    }

    public void setPayeeInfo(PayeeInfo payeeInfo) {
        this.payeeInfo = payeeInfo;
    }

    public PayerInfo getPayerInfo() {
        return payerInfo;
    }

    public void setPayerInfo(PayerInfo payerInfo) {
        this.payerInfo = payerInfo;
    }

    public String getOriOrderNo() {
        return oriOrderNo;
    }

    public void setOriOrderNo(String oriOrderNo) {
        this.oriOrderNo = oriOrderNo;
    }

    public String getOutOrderNo() {
        return outOrderNo;
    }

    public void setOutOrderNo(String outOrderNo) {
        this.outOrderNo = outOrderNo;
    }

    @Override
    public String toString() {
        return "BankQueryReqDTO{" +
                "oriOrderNo='" + oriOrderNo + '\'' +
                ", outOrderNo='" + outOrderNo + '\'' +
                "} " + super.toString();
    }
}
