package com.ylli.api.user.mapper;

import com.ylli.api.user.model.UserKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface UserKeyMapper extends Mapper<UserKey> {
    @Select("SELECT t_user_key.secret_key FROM t_user_base LEFT JOIN t_user_key ON t_user_base.user_id = t_user_key.user_id WHERE t_user_base.merchant_no = #{mch_id}")
    String getKeyByMchId(@Param("mach_id") String mchId);

    @Select("SELECT t_user_key.secret_key FROM t_user_key WHERE user_id = #{id}")
    String getKeyById(@Param("id") Long mchId);
}
