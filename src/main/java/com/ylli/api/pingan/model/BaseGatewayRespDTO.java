package com.ylli.api.pingan.model;


import java.io.Serializable;

/**
 * *********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.gateway.dto
 * @Author: yangdf
 * @Date 2018/5/8 16:46
 * @Description:
 * @ModifyDetail
 * @ModifyDate
 */
public class BaseGatewayRespDTO implements Serializable {
    private static final long serialVersionUID = -8861553356406595260L;

    /***返回编码**/
    private String respCode;
    /***返回编码描述**/
    private String respDesc;
    /***全局流水***/
    private String globalSeq;
    /***交易状态**/
    private String status;
    /***渠道返回编码**/
    private String channelRespCode;
    /***渠道返回编码描述**/
    private String channelRespDesc;

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespDesc() {
        return respDesc;
    }

    public void setRespDesc(String respDesc) {
        this.respDesc = respDesc;
    }

    public String getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(String globalSeq) {
        this.globalSeq = globalSeq;
    }

    public void setRespDTO(ErrorCode errorCode, String globalSeq) {
        this.respCode = errorCode.getErrCode();
        this.respDesc = errorCode.getErrMsg();
        this.globalSeq = globalSeq;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChannelRespCode() {
        return channelRespCode;
    }

    public void setChannelRespCode(String channelRespCode) {
        this.channelRespCode = channelRespCode;
    }

    public String getChannelRespDesc() {
        return channelRespDesc;
    }

    public void setChannelRespDesc(String channelRespDesc) {
        this.channelRespDesc = channelRespDesc;
    }

    @Override
    public String toString() {
        return "BaseGatewayRespDTO{" +
                "respCode='" + respCode + '\'' +
                ", respDesc='" + respDesc + '\'' +
                ", globalSeq='" + globalSeq + '\'' +
                ", status='" + status + '\'' +
                ", channelRespCode='" + channelRespCode + '\'' +
                ", channelRespDesc='" + channelRespDesc + '\'' +
                '}';
    }
}
