package com.ylli.api.third.pay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_qr_pend_info")
public class QrPendInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    public Integer money;

    public Boolean enable;

    public Timestamp orderTime;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
