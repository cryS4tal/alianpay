package com.ylli.api.wallet.mapper;

import com.ylli.api.wallet.model.CashLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface CashLogMapper extends Mapper<CashLog> {

    List<CashLog> cashList(@Param("mch_id") Long mchId,
                           @Param("phone") String phone);
}
