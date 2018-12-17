package com.ylli.api.pingan.model;


/**
 * **********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.core.enums
 * @Author: yangdf
 * @Date 2018/5/7 16:10
 * @Description: 错误码为6位
 * 第一位：错误类型，0-成功，2-失败，3-处理中，4-参数错误，5-权限错误，6-签名错误，9-异常
 * 第二三位：系统，09-渠道网关
 * 第四五六位：具体码值001-999
 * 公共错误码：000000-成功，200000-失败，300000-处理中，400000-参数错误（具体的参数错误，由各系统自定义），507001-权限不足，507002-接口未开放，607001-签名错误
 * @ModifyDetail
 * @ModifyDate
 */
public enum BankGatewayErrorCodeEnum implements ErrorCode {
    /***调用渠道网关异常**/
    DUBBO_EXCEPTION("909001", "调用渠道网关异常"),
    /***调用渠道异常**/
    SEND_CHANNEL_EXCEPTION("909002", "调用渠道异常"),
    /***组装渠道报文异常**/
    CREATE_MESSAGE_EXCEPTION("909003", "组装渠道报文异常"),
    /***解析渠道报文异常**/
    RESULT_MESSAGE_EXCEPTION("909004", "解析渠道报文异常"),
    /***验签失败**/
    VERIFY_SIGN_FAIL("906001", "验签失败"),
    /***未找到对应状态**/
    CHANNEL_STATUS_UNKNOWN("903001", "未找到对应状态");

    private String errCode;
    private String errMsg;

    BankGatewayErrorCodeEnum(String errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    @Override
    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
