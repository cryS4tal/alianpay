package com.ylli.api.pay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_sys_channel")
public class SysChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String code;

    public String name;

    public Boolean state;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
