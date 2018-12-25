package com.ylli.api.mch.model;


import java.util.List;

public class MchAgentDto {
    public String phone;

    public String password;

    public String mchName;

    public List<MchApp> apps;

    public Long mchId;

    public List<MchAppDetail> userAppDetail;
}
