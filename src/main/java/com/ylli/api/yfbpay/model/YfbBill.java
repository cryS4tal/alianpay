package com.ylli.api.yfbpay.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_yfb_bill")
public class YfbBill {

    public static final Integer NEW = 1;
    //请求处理中..
    public static final Integer ING = 2;
    public static final Integer FINISH = 3;
    public static final Integer FAIL = 4;
    public static final Integer AUTO_CLOSE = 9;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    //平台订单编号.
    public String orderNo;

    //商户订单号;
    public String subNo;

    //上游订单号
    public String superNo;

    public Integer amount;

    //订单状态（new 1 / ing 2 / finish 3 / cancel 4）
    public Integer status;

    public String memo;     //not required

    public String notifyUrl;

    public String redirectUrl;

    //易付宝系统返回订单结果信息.
    public String msg;

    //子系统是否接受通知.
    public Boolean isSuccess;

    //支付类型：支付宝，微信
    public String payType;

    //支付方式：扫码，wap，app..
    public String tradeType;

    //交易成立时间
    public Timestamp tradeTime;

    /**
     * todo
     */
    //分润金额
    public Double bonusMoney;

    public Timestamp createTime;

    public Timestamp modifyTime;

    public static String statusToString(int status) {
        if (status == NEW) {
            return "未支付";
        } else if (status == ING) {
            return "交易进行中";
        } else if (status == FINISH) {
            return "交易成功";
        } else if (status == FAIL) {
            return "交易失败";
        } else if (status == AUTO_CLOSE) {
            return "超时关闭";
        } else {
            return "状态异常";
        }
    }

}
