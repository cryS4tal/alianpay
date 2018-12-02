package com.ylli.api.wallet.mapper;

import com.ylli.api.wallet.model.Wallet;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface WalletMapper extends Mapper<Wallet> {
    @Select("SELECT * FROM t_wallet WHERE user_id = ${user_id}")
    Wallet selectByUserId(@Param("user_id") Long userId);
}
