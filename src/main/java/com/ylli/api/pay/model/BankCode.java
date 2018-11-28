package com.ylli.api.pay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 银行编码表
 */
@Table(name = "t_bank_code")
public class BankCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //通道类型：各通道首字母大写
    public String type;
    //银行代码
    public String code;
    //名称
    public String name;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
