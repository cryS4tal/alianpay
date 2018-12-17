package com.ylli.api.pingan.model;

import java.io.Serializable;

/**
 * 单笔付款结果查询
 */
public class ReqKHKF04 implements Serializable {
    private static final long serialVersionUID = -3784612800410825403L;

    private String AcctNo;//企业签约帐号
    private String OrderNumber;//订单号
    private String BussFlowNo;//银行业务流水号

    public String getAcctNo() {
        return AcctNo;
    }

    public void setAcctNo(String acctNo) {
        AcctNo = acctNo;
    }

    public String getOrderNumber() {
        return OrderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        OrderNumber = orderNumber;
    }

    public String getBussFlowNo() {
        return BussFlowNo;
    }

    public void setBussFlowNo(String bussFlowNo) {
        BussFlowNo = bussFlowNo;
    }
}
