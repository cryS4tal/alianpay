package com.ylli.api.sys.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 系统代付渠道
 */
@Table(name = "t_bank_payment")
public class BankPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String code;

    public String name;

    public Boolean state;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
