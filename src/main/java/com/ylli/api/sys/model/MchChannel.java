package com.ylli.api.sys.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_mch_channel")
public class MchChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long mchId;

    public Long channelId;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
