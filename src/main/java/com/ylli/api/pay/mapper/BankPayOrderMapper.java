package com.ylli.api.pay.mapper;

import com.ylli.api.pay.model.BankPayOrder;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface BankPayOrderMapper extends Mapper<BankPayOrder> {
    @Select("SELECT * FROM t_bank_pay_order WHERE sys_order_id = #{sys_order_id}")
    BankPayOrder selectBySysOrderId(@Param("sys_order_id") String sysOrderId);

    @Select("SELECT * FROM t_bank_pay_order WHERE mch_order_id = #{mch_order_id}")
    BankPayOrder selectByMchOrderId(@Param("mch_order_id") String mchOrderId);

    List<BankPayOrder> getOrders(@Param("mch_ids") List<Long> mchIds,
                                 @Param("status") Integer status,
                                 @Param("mch_order_id") String mchOrderId,
                                 @Param("sys_order_id") String sysOrderId,
                                 @Param("acc_name") String accName,
                                 @Param("pay_type") Integer payType,
                                 @Param("trade_time") Date tradeTime,
                                 @Param("start_time") Date startTime,
                                 @Param("end_time") Date endTime);
}
