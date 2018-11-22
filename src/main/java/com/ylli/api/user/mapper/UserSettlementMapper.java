package com.ylli.api.user.mapper;

import com.ylli.api.user.model.UserSettlement;
import java.sql.Timestamp;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface UserSettlementMapper extends Mapper<UserSettlement> {

    @Select("SELECT * FROM t_user_settlement WHERE user_id = ${user_id}")
    UserSettlement selectByUserId(@Param("user_id") Long userId);

    List<UserSettlement> selectByCondition(@Param("user_id") Long userId,
                                           @Param("name") String name,
                                           @Param("identity_card") String identityCard,
                                           @Param("bankcard_number") String bankcardNumber,
                                           @Param("reserved_phone") String reservedPhone,
                                           @Param("open_bank") String openBank,
                                           @Param("sub_bank") String subBank,
                                           @Param("begin_time") Timestamp beginTime,
                                           @Param("end_time") Timestamp endTime);

}
