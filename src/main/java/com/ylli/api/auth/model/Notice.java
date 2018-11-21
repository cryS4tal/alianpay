package com.ylli.api.auth.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by yqpeng on 2017/3/17.
 */
@Table(name = "t_notice")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String type;
    public Long outId;
    public Long ownerId;
    public String title;
    public String description;
    public String extras;
    public Integer state;
    public Timestamp createTime;
    public Timestamp modifyTime;
}
