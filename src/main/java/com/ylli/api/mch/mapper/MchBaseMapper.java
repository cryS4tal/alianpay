package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.MchBase;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface MchBaseMapper extends Mapper<MchBase> {

    @Select("SELECT * FROM t_user_base WHERE mch_id = ${mch_id}")
    MchBase selectByMchId(@Param("mch_id") Long mchId);

    List<MchBase> getBase(@Param("mch_id") Long mchId,
                          @Param("state") Integer state,
                          @Param("mch_name") String mchName,
                          @Param("name") String name,
                          @Param("phone") String phone,
                          @Param("license") String businessLicense);

    @Select("select mch_name from t_user_base where mch_name like #{mch_name}")
    List<String> getMchNameLike(@Param("mch_name") String mchName);
}
