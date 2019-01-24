package com.ylli.api.wallet.mapper;

import com.ylli.api.wallet.model.WalletLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface WalletLogMapper extends Mapper<WalletLog> {

    @Select("SELECT * FROM t_wallet_log WHERE mch_id = ${mch_id} ORDER BY id DESC")
    List<WalletLog> selectByMchId(@Param("mch_id") Long mchId);
}
