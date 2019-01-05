package com.ylli.api.wallet.model;

import java.sql.Timestamp;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name = "t_cash_log")
public class CashLog {

    public static final Integer NEW = 0;
    public static final Integer FINISH = 1;
    public static final Integer FAILED = 2;

    public static final Integer PROCESS = 9;


    /**
     * 代付类型
     * 1 - 手工
     * 2 - 平安
     * 3 - 先锋
     * 4 - CNT
     */
    public static final Integer MANUAL = 1;
    public static final Integer PINGAN = 2;
    public static final Integer XIANFENG = 3;
    public static final Integer CNT = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Transient
    public String mchName;
    public Long mchId;
    public Integer money;

    //格式：（xx银行）
    public String openBank;
    //格式：（安徽省芜湖市黄山路支行）
    public String subBank;
    public String bankcardNumber;
    public String name;
    //非必需
    public String identityCard;
    public String reservedPhone;

    public Integer state;

    public String msg;

    //提现类型（1-手动代付，2-平安代付，3-先锋代付）
    public Integer type;

    public Timestamp createTime;

    public Timestamp modifyTime;

    public static String stateFormat(Integer state) {
        if (state == CashLog.NEW) {
            return "待处理";
        } else if (state == CashLog.FINISH) {
            return "成功";
        } else if (state == CashLog.FAILED) {
            return "失败";
        } else if (state == CashLog.PROCESS) {
            return "进行中";
        } else {
            return "异常";
        }
    }
}
