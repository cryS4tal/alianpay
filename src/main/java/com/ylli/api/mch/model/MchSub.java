package com.ylli.api.mch.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "t_mch_sub")
public class MchSub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long mchId;

    @Transient
    public String mchName;

    public Long subId;

    @Transient
    public String subName;

    //代理商类型：1-支付，2-代付
    public Integer type;

    //支付宝费率差
    public Integer alipayRate;

    @Transient
    public Integer supAlipayRate;

    @Transient
    public Integer subAlipayRate;

    //微信费率差
    public Integer wxRate;

    @Transient
    public Integer supWxRate;

    @Transient
    public Integer subWxRate;

    //代付费率差
    public Integer bankRate;

    @Transient
    public Integer supRate;

    @Transient
    public Integer subRate;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
