package com.ylli.api.example.model;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by RexQian on 2017/5/10.
 */
@Table(name = "t_example")
public class Example {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    public Boolean enabled;

    public List<Long> images;

    public List<String> imageUrls;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
