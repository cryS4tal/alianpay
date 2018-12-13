package com.ylli.api.wallet.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_wz_cash_log")
public class WzCashLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long logId;

    public Integer failCount;

    public String errcode;

    public String errmsg;
}
