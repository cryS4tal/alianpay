package com.ylli.api.auth.model;

import java.sql.Timestamp;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by ylli On 2018/11/20.
 */

@Table(name = "t_password")
public class Password {

    @Id
    public Long id;

    public String password;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
