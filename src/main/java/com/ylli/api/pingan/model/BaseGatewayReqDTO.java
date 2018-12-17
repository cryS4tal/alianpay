package com.ylli.api.pingan.model;

import java.io.Serializable;
import java.util.Date;

/**
 * *********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.gateway.dto
 * @Author: yangdf
 * @Date 2018/5/8 16:50
 * @Description: 公共请求入参
 * @ModifyDetail
 * @ModifyDate
 */
public class BaseGatewayReqDTO implements Serializable {
    private static final long serialVersionUID = -8492547755750430799L;
    /***全局流水**/
    private String globalSeq;
    /***渠道商户号**/
    private String channelMerchantId;
    /***请求订单号**/
    private String orderNo;
    /***请求时间**/
    private Date tradeTime;
    /**
     * 签名相关信息
     **/
    private SecurityInfo securityInfo;

    public String getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(String globalSeq) {
        this.globalSeq = globalSeq;
    }

    public String getChannelMerchantId() {
        return channelMerchantId;
    }

    public void setChannelMerchantId(String channelMerchantId) {
        this.channelMerchantId = channelMerchantId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Date getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
    }

    public SecurityInfo getSecurityInfo() {
        return securityInfo;
    }

    public void setSecurityInfo(SecurityInfo securityInfo) {
        this.securityInfo = securityInfo;
    }

    @Override
    public String toString() {
        return "BaseGatewayReqDTO{" +
                "globalSeq='" + globalSeq + '\'' +
                ", channelMerchantId='" + channelMerchantId + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", tradeTime=" + tradeTime +
                ", securityInfo=" + securityInfo.toString() +
                '}';
    }
}
