package com.ylli.api.user.model;

import java.sql.Timestamp;

public class UserAppDetail {

    public Long mchId;

    public String mchName;

    //appId 对应 商户应用id，userAppId
    public Long appId;

    public String appName;

    //应用状态：true = 启用
    public Integer rate;

    public Timestamp createTime;
}
