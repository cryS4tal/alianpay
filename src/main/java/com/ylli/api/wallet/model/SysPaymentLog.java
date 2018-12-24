package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_sys_payment_log")
public class SysPaymentLog {

    public static final String PINGAN = "PingAn";

    public static final String XIANFEN = "XianFen";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //退款单号
    public String orderId;

    public String type;

    public Integer failCount;

    public String errcode;

    public String errmsg;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
