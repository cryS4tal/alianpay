package com.ylli.api.auth.model;

import java.sql.Timestamp;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by ylli on 2018/11/20.
 */
@Table(name = "t_phone_auth")
public class PhoneAuth {
    @Id
    public Long id;

    public String phone;
    public Timestamp createTime;
    public Timestamp modifyTime;
}
