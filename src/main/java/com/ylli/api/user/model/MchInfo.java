package com.ylli.api.user.model;

import java.sql.Timestamp;

/**
 * 商户信息detail.
 */
public class MchInfo {

    public Long mchId;

    public String phone;
    //密钥
    public String secret;
    //结算类型
    public Integer chargeType;
    //结算率
    public Integer chargeRate;
    //创建时间
    public Timestamp createTime;

    public MchInfo(Long mchId, String phone, String secret, Integer chargeType, Integer chargeRate, Timestamp createTime) {
        this.mchId = mchId;
        this.phone = phone;
        this.secret = secret;
        this.chargeType = chargeType;
        this.chargeRate = chargeRate;
        this.createTime = createTime;
    }

    public MchInfo() {
    }
}
