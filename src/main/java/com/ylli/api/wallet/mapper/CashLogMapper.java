package com.ylli.api.wallet.mapper;

import com.ylli.api.wallet.model.CashLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface CashLogMapper extends Mapper<CashLog> {

    List<CashLog> cashList(@Param("mch_id") Long mchId,
                           @Param("phone") String phone);

    Long selectCashMoney(@Param("mch_id") Long mchId);

    @Select("SELECT DISTINCT open_bank,sub_bank,bankcard_number,`name` FROM t_cash_log WHERE state = 1 AND mch_id = ${mch_id} ORDER BY id DESC LIMIT 10")
    List<CashLog> selectBankList(@Param("mch_id") long authId);
}
