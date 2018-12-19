package com.ylli.api.mch.model;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_user_base")
public class UserBase {

    public static final Integer NEW = 0;
    public static final Integer PASS = 1;
    public static final Integer FAIL = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    //商户号
    public Long mchId;

    //商户名称
    public String mchName;

    //简称
    public String nickName;

    //联系人邮箱
    public String email;

    //法人姓名
    public String legalName;

    //法人手机
    public String legalPhone;

    //联系人
    public String linkName;

    //联系人手机
    public String linkPhone;

    //组织机构代码
    public String orgCode;

    //营业执照
    public String businessLicense;

    //身份证照片
    public List<Long> cardImages;

    //营业执照
    public List<Long> licenseImages;

    //其他照片
    public List<Long> otherImages;

    //审核状态
    public Integer state;

    public Timestamp createTime;

    public Timestamp modifyTime;
}
