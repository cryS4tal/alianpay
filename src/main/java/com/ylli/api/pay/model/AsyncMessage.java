package com.ylli.api.pay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_async_message")
public class AsyncMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long billId;

    public String bankPayOrderId;

    public String url;

    public String params;

    public Integer failCount;

    public String errcode;

    public String errmsg;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
