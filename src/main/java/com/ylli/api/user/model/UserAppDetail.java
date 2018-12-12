package com.ylli.api.user.model;

import java.sql.Timestamp;

public class UserAppDetail {

    public Long id;

    public Long mchId;

    public String mchName;

    public Long appId;

    public String appName;

    //应用状态：true = 启用
    public Integer rate;

    public Boolean isDefault;

    public Timestamp createTime;
}
