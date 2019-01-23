package com.ylli.api.mch.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_mch_sub")
public class MchSub {

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

    public Timestamp createTime;

    public Timestamp modifyTime;
}
