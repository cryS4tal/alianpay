package com.ylli.api.xfpay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 先锋支付 银行编码表
 */
@Table(name = "t_xf_bank")
public class XfBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //银行代码
    public String code;
    //名称
    public String name;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
