package com.ylli.api.user.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_cash_log")
public class CashLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    public Integer money;

    public Boolean isOk;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
