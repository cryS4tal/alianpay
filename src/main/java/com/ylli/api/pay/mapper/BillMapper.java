package com.ylli.api.pay.mapper;

import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.model.SumAndCount;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface BillMapper extends Mapper<Bill> {

    List<Bill> getBills(@Param("mch_id") Long userId,
                        @Param("status") Integer status,
                        @Param("mch_order_id") String mchOrderId,
                        @Param("sys_order_id") String sysOrderId,
                        @Param("pay_type") String payType,
                        @Param("trade_type") String tradeType,
                        @Param("trade_time") Date tradeTime,
                        @Param("start_time") Date startTime,
                        @Param("end_time") Date endTime);

    @Select("SELECT SUM(money) as total,COUNT(*) as count FROM t_bill WHERE mch_id = ${mch_id} AND status = 3 AND DAYOFYEAR(NOW()) = DAYOFYEAR(trade_time)")
    SumAndCount getTodayDetail(@Param("mch_id") Long mchId);

    @Update("UPDATE t_bill SET status = 9 WHERE status = 1 AND DATE_ADD(create_time,INTERVAL 9 HOUR) < NOW()")
    Integer autoClose();
}
