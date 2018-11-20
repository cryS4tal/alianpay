package com.ylli.api.alipay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_notify")
public class Notify {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //第三方回调url
    public String url;

    //回调参数
    public String params;

    //失败次数
    public Integer failCount;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
