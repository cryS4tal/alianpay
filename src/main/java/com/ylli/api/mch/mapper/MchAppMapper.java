package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.MchApp;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface MchAppMapper extends Mapper<MchApp> {

    @Select("SELECT * FROM t_user_app WHERE mch_id = ${mch_id}")
    List<MchApp> selectAppsByMchId(@Param("mch_id") Long mchId);
}
