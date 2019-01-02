package com.ylli.api.mch.model;

/**
 * 商户列表信息
 */
public class Mch {
    public Long mchId;

    public String mchName;

    public String phone; //注册手机

    public Integer money;   //商户余额

    public Integer reservoir; //代付余额

    public Integer auditState; //审核状态

    public String mchState; //账户状态

    public Long channelId;  //通道id

    public String channelName;  //通道名称
}
