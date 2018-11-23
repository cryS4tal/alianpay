package com.ylli.api.storage.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by RexQian on 2017/3/14.
 */
@Table(name = "t_storage")
public class Storage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String path;

    public String contentType;

    public Long committerId;

    public String name;

    public String meta;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
