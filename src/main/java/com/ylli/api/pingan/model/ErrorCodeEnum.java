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
 * 第二三位：系统，01-快捷，02-网关，03-代发，04-代扣，05-管理系统，06-app，07-开放平台,08-路由,09-渠道网关,10-退款
 * 第四五六位：具体码值001-999
 * 公共错误码：000000-成功，200000-失败，300000-处理中，400000-参数错误（具体的参数错误，由各系统自定义），507001-权限不足，507002-接口未开放，607001-签名错误
 * @ModifyDetail
 * @ModifyDate
 */
public enum ErrorCodeEnum implements ErrorCode {
    /***成功**/
    SUCCESS("000000", "成功"),
    /***交易处理中**/
    PROCESSING("300000", "交易处理中"),
    /***失败**/
    FAIL("200000", "失败"),
    /***交易处理中，请勿重复支付**/
    TRADE_PROCESSING("200001", "交易处理中，请勿重复支付"),
    /***获取渠道信息失败**/
    GET_CHANNEL_ERROR("200002", "获取渠道信息失败"),
    /***交易终态，请勿重复处理**/
    TRADE_STATUS_FINAL("200003", "交易终态，请勿重复处理"),
    /***获取商户产品信息失败**/
    GET_MERCHANT_PRODUCT_ERROR("200004", "获取商户产品信息失败"),
    /***银行账户状态异常***/
    BANK_ACCOUNT_STATUS_WRONG("200005", "银行账户状态异常"),
    /***银行账户余额不足***/
    BANK_ACCOUNT_INSUFFICIENT_BALANCE("200006", "银行账户余额不足"),
    /***绑卡数量最大,无法继续绑卡***/
    BANK_CARD_BIND_MAX("200007", "绑卡数量最大,无法继续绑卡"),
    /***日绑卡失败次数最大,请明天再试***/
    BANK_CARD_BIND_DAYLY_FAIL_MAX("200008", "日绑卡失败次数最大,请明天再试"),
    /***日绑卡次数最大,请明天再试***/
    BANK_CARD_BIND_DAYLY_SUCCESS_MAX("200009", "日绑卡次数最大,请明天再试"),
    /***黑名单用户***/
    USER_IN_BLACKLIST("200010", "黑名单用户"),
    /***银行卡信息不匹配**/
    BANKCARD_NOT_MATCH("200015", "银行卡信息不匹配"),
//    /***黑名单用户***/
//    MOUNT_OUT_OF_CHANNEL_LIMIT("200011", "金额超出渠道限额"),
    /***未开通认证支付**/
    BANK_CARD_NOT_OPEN("200017", "未开通认证支付"),
    /***协议号失效，请解绑后重新绑卡**/
    BQP_AGREEMENT_OUT_DATE("200018", "协议号失效，请解绑后重新绑卡"),
    /***系统异常**/
    EXCEPTION("999999", "系统异常"),
    /***参数错误**/
    PARAMS_ERROR("400000", "参数错误"),
    /***订单号不存在**/
    ORDER_NO_NOT_EXIST("400001", "订单号不存在"),
    /***支付平台交易订单号不存在**/
    PAY_SERIAL_NO_NOT_EXIST("400002", "支付平台交易订单号不存在"),
    /***交易不存在**/
    TRADE_NOT_EXIST("400003", "交易不存在"),
    /***订单号重复**/
    ORDER_NO_REPEAT("400004", "订单号重复"),
    /***订单号与支付平台订单号不能同时为空**/
    ORDER_NO_NOT_ALL_EMPTY("400005", "订单号与支付平台订单号不能同时为空"),
    /***发送mq参数错误**/
    ROCKET_MQ_PARAMS_ERROR("400006", "发送mq参数错误"),
    /***商户上传日期格式错误**/
    MER_TRADE_TIME_WRONG("400007", "商户上传日期格式错误"),
    /***交易类型错误**/
    ORDER_TRADE_TYPE_WRONG("400008", "交易类型错误"),
    /*** 商户渠道可用金额不足**/
    CHANNEL_ACCOUNT_AMOUNT_DEFICIENCY("400009", "商户渠道可用金额不足"),
    /***联行号不能为空**/
    BANK_BRANCH_ID_NOT_NULL("400010", "联行号不能为空"),
    /***银行编号不能为空**/
    BANK_CODE_NOT_NULL("400011", "银行编号不能为空"),
    /***快捷签约请求信息不存在**/
    BQP_SIGN_APPLY_NOT_EXIST("400012", "快捷签约请求信息不存在"),
    /***快捷签约信息不存在**/
    BQP_SIGN_NOT_EXIST("400013", "快捷签约信息不存在"),
    /***短信验证码错误**/
    VERCODE_WRONG("400014", "短信验证码错误"),
    /***快捷信息校验不通过**/
    BQP_INFO_WRONG("400015", "快捷用户信息校验不通过"),
    /***代扣信息校验不通过**/
    DEDUCT_INFO_WRONG("400016", "代扣用户信息校验不通过"),
    /***权限不足**/
    PERMISSION_DENIED("507001", "权限不足"),
    /***接口未开放**/
    INTERFACE_NOT_OPEN("507002", "接口未开放"),
    /***签名错误**/
    SIGN_ERROR("607001", "签名错误");

    ErrorCodeEnum(String errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private String errCode;
    private String errMsg;

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
