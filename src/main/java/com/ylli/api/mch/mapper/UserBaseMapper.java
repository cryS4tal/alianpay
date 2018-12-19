package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.UserBase;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface UserBaseMapper extends Mapper<UserBase> {
    @Select("SELECT * FROM t_user_base WHERE mch_id = ${mch_id}")
    UserBase selectByMchId(@Param("mch_id") Long mchId);

    List<UserBase> getBase(@Param("mch_id") Long mchId,
                           @Param("state") Integer state,
                           @Param("mch_name") String mchName,
                           @Param("name") String name,
                           @Param("phone") String phone,
                           @Param("license") String businessLicense);
}
