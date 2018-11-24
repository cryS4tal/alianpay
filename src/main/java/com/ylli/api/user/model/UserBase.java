package com.ylli.api.user.model;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_user_base")
public class UserBase {

    public static final Integer COMPANY = 1;
    public static final Integer PERSON = 2;

    public static final Integer PASS = 1;
    public static final Integer FAIL = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    public Integer userType;

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

    //营业执照
    public String businessLicense;

    public String legalPerson;

    public String legalPhone;

    //审核状态
    public Integer state;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
