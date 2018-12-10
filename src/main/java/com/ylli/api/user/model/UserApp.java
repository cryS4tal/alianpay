package com.ylli.api.user.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_user_app")
public class UserApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long mchId;

    public Long appId;

    //应用状态：true = 启用
    public Integer rate;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
