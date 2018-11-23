package com.ylli.api.user.model;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_user_base")
public class UserBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    //商户号
    public String merchantNo;

    public String name;

    public String identityCard;

    public String phone;

    public String email;

    //个人用户对应身份证照片，企业用户对应营业执照。
    public List<Long> images;

    /**
     * 公司字段
     */
    public String companyName;

    public String address;

    public String businessLicense;

    public String legalPerson;

    public String legalPhone;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
