package com.ylli.api.auth.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by yqpeng on 2017/2/20.
 */
@Table(name = "t_department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Integer type;
    public String name;
    public String description;
    public Timestamp createTime;
    public Timestamp modifyTime;
}
