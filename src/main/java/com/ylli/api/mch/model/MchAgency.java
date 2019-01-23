package com.ylli.api.mch.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "t_mch_agency")
public class MchAgency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long mchId;

    public Long subId;

    //代理商类型：1-支付，2-代付
    public Integer type;

    //支付宝费率差
    public Integer alipayRate;

    //微信费率差
    public Integer wxRate;

    //代付费率差
    public Integer bankRate;

    @Transient
    public String mchName;

    @Transient
    public String subName;

    @Transient
    public Integer supAlipayRate;

    @Transient
    public Integer subAlipayRate;

    @Transient
    public Integer supWxRate;

    @Transient
    public Integer subWxRate;

    @Transient
    public Integer supRate;

    @Transient
    public Integer subRate;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
