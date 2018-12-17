package com.ylli.api.pingan.model;

/**
 * **********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.gateway.dto
 * @Author: yangdf
 * @Date 2018/5/8 16:58
 * @Description: 支付返回结果
 * @ModifyDetail
 * @ModifyDate
 */
public class BankPayRespDTO extends BaseGatewayRespDTO {

    private static final long serialVersionUID = -7648939233946082812L;

    /**
     * 渠道返回的流水号
     */
    private String outSerialNo;

    public String getOutSerialNo() {
        return outSerialNo;
    }

    public void setOutSerialNo(String outSerialNo) {
        this.outSerialNo = outSerialNo;
    }

    @Override
    public String toString() {
        return "BankPayRespDTO [outSerialNo=" + outSerialNo + super.toString() + "]";
    }
}
