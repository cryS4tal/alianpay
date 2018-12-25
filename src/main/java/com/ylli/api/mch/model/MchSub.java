package com.ylli.api.mch.model;

import javax.persistence.Table;
import java.sql.Timestamp;

@Table(name = "t_mch_sub")
public class MchSub {
    public Long mchId;

    public Long subId;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
