package com.ylli.api.user.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_sys_app")
public class SysApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String appName;

    //统一费率
    public Integer rate;

    //应用状态：true = 启用
    public Boolean status;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
