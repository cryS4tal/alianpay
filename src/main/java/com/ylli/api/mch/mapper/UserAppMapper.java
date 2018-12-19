package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.UserApp;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface UserAppMapper extends Mapper<UserApp> {

    @Select("SELECT * FROM t_user_app WHERE mch_id = ${mch_id}")
    List<UserApp> selectAppsByMchId(@Param("mch_id") Long mchId);
}
