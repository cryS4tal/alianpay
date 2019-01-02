package com.ylli.api.pay.mapper;

import com.ylli.api.pay.model.AsyncMessage;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface AsyncMessageMapper extends Mapper<AsyncMessage> {

    @Select("SELECT * FROM t_async_message WHERE bank_pay_order_id IS NULL")
    List<AsyncMessage> selectAllBill();

    @Select("SELECT * FROM t_async_message WHERE bill_id IS NULL")
    List<AsyncMessage> selectAllOrder();

}
