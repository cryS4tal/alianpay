package com.ylli.api.mch.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_user_app")
public class MchApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long mchId;

    public Long appId;

    public Integer rate;

    public Timestamp createTime;

    public Timestamp modifyTime;
}