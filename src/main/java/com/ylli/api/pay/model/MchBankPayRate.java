package com.ylli.api.pay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_mch_bank_pay_rate")
public class MchBankPayRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long mchId;

    public Integer rate;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
