package com.ylli.api.yfbpay.mapper;

import com.ylli.api.pay.model.SumAndCount;
import com.ylli.api.yfbpay.model.YfbBill;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface YfbBillMapper extends Mapper<YfbBill> {
    List<YfbBill> getBills(@Param("user_id") Long userId,
                           @Param("status") Integer status,
                           @Param("mch_order_id") String mchOrderId,
                           @Param("sys_order_id") String sysOrderId,
                           @Param("pay_type") String payType,
                           @Param("trade_type") String tradeType,
                           @Param("trade_time") Date tradeTime,
                           @Param("start_time") Date startTime,
                           @Param("end_time") Date endTime);

    @Select("SELECT SUM(amount) as total,COUNT(*) as count FROM t_yfb_bill WHERE user_id = ${user_id} AND status = 3")
    SumAndCount getTodayDetail(@Param("user_id") Long userId);

    @Select("SELECT SUM(amount) FROM t_yfb_bill WHERE user_id = ${user_id} AND status = 3")
    Integer getMaxCash(@Param("user_id") Long userId);

    @Update("UPDATE t_yfb_bill SET status = 9 WHERE status = 1 AND DATE_ADD(create_time,INTERVAL 10 HOUR) < NOW()")
    void closeExpiredBill();
}
