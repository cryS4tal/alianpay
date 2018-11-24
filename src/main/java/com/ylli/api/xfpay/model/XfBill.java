package com.ylli.api.xfpay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_xf_bill")
public class XfBill {

    public static final Integer NEW = 1;
    //请求处理中..
    public static final Integer ING = 2;
    public static final Integer FINISH = 3;
    public static final Integer FAIL = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    //订单编号.
    public String orderNo;

    public Integer amount;

    //订单状态（new / finish / cancel）
    public Integer status;

    //商户订单号;
    public String  subNo;

    //先锋订单号
    public String superNo;


    /**
     * 以下字段带记录.
     * sql 需要加入
     *
     *
     *
     *
     */
    public String accountNo;
    public String accountName;
    public String mobileNo; //not required
    public String bankNo;
    public Integer userType;
    /**
     * 1（借记卡）
     * 2（贷记卡）
     * 4（对公账户）
     * not required.
     *
     *  userType = 1; accountType = (1,2) 默认1
     *  userType = 2; accountType 默认4
     */
    public Integer accountType;
    public String memo;     //not required


    public Timestamp createTime;

    public Timestamp modifyTime;

}
