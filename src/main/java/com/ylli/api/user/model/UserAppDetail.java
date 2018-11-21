package com.ylli.api.user.model;

import java.sql.Timestamp;

public class UserAppDetail {

    public Long id;

    public Long userId;

    public String nickname;

    public String appId;

    public String appName;

    //应用状态：true = 启用
    public Boolean status;

    public Timestamp createTime;

    public Timestamp modifyTime;

}
