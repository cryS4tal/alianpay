package com.ylli.api.mch.model;


import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Table(name = "t_mch_agent")
public class MchAgent {
    @Id
    public Long mchId;
    public String mchName;
    public String linkPhone;
    public Timestamp createTime;
    public Timestamp modifyTime;
}
