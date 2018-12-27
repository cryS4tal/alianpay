package com.ylli.api.pay.mapper;

import com.ylli.api.pay.model.BankPayOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface BankPayOrderMapper extends Mapper<BankPayOrder> {
    @Select("SELECT * FROM t_bank_pay_order WHERE sys_order_id = #{sys_order_id}")
    BankPayOrder selectBySysOrderId(@Param("sys_order_id") String sysOrderId);
}
