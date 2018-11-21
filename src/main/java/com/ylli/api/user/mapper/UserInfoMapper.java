package com.ylli.api.user.mapper;

import com.ylli.api.user.model.UserInfo;
import java.sql.Timestamp;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface UserInfoMapper extends Mapper<UserInfo> {

    @Select("SELECT * FROM t_user_info WHERE user_id = ${user_id}")
    UserInfo selectByUserId(@Param("user_id") Long userId);

    List<UserInfo> selectByCondition(@Param("user_id") Long userId,
                                     @Param("name") String name,
                                     @Param("identity_card") String identityCard,
                                     @Param("bankcard_number") String bankcardNumber,
                                     @Param("reserved_phone") String reservedPhone,
                                     @Param("open_bank") String openBank,
                                     @Param("sub_bank") String subBank,
                                     @Param("begin_time") Timestamp beginTime,
                                     @Param("end_time") Timestamp endTime);

}
