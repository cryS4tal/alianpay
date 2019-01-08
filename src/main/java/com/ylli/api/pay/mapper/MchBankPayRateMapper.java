package com.ylli.api.pay.mapper;

import com.ylli.api.pay.model.MchBankPayRate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface MchBankPayRateMapper extends Mapper<MchBankPayRate> {

    @Select("SELECT * FROM t_mch_bank_pay_rate WHERE mch_id = ${mch_id}")
    MchBankPayRate selectByMchId(@Param("mch_id") Long mchId);
}
