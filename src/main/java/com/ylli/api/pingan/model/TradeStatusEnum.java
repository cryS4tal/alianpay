package com.ylli.api.pingan.model;

/**
 * *********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.core.enums
 * @Author: yangdf
 * @Date 2018/5/9 14:49
 * @Description: 交易状态枚举
 * @ModifyDetail
 * @ModifyDate
 */
public enum TradeStatusEnum {
    /***成功**/
    SUCCESS("SUCCESS", "成功"),
    /***失败**/
    FAIL("FAIL", "失败"),
    /***待支付**/
    WAIT_PAY("WAIT_PAY", "待支付"),
    /***处理中**/
    PROCESSING("PROCESSING", "处理中"),
    /***已退款**/
    HAD_REFUNDED("REFUNDED", "已退款"),
    /***关闭**/
    CLOSED("CLOSED", "关闭");

    //状态描述
    public final static String TRADE_STATUS_STR = "WAIT_PAY-待支付,PROCESSING-处理中,SUCCESS-成功,FAIL-失败,REFUND_FINISH-退款完成,CLOSED-关闭";
    //状态验证正则
    public final static String TRADE_STATUS_REGEXP = "(WAIT_PAY)|(PROCESSING)|(SUCCESS)|(FAIL)|(REFUND_FINISH)|(CLOSED)";

    TradeStatusEnum(String status, String statusDesc) {
        this.status = status;
        this.statusDesc = statusDesc;
    }

    private String status;
    private String statusDesc;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public static TradeStatusEnum getStatusEnum(String status) {
        for (TradeStatusEnum statusEnum : TradeStatusEnum.values()) {
            if (statusEnum.status.equals(status)) {
                return statusEnum;
            }

        }
        return null;
    }
}
