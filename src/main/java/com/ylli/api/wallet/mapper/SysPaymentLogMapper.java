package com.ylli.api.wallet.mapper;

import com.ylli.api.wallet.model.SysPaymentLog;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SysPaymentLogMapper extends Mapper<SysPaymentLog> {

   @Select("SELECT * FROM t_sys_payment_log WHERE fail_count < 10 AND type = 'PingAn'")
    List<SysPaymentLog> selectProcess();
}
