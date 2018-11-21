package com.ylli.api.user.mapper;

import com.ylli.api.user.model.UserApp;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface UserAppMapper extends Mapper<UserApp> {
    List<UserApp> selectApps(@Param("user_id") Long userId);

    @Select("SELECT * FROM t_user_app WHERE app_id = ${app_id}")
    UserApp selectByAppId(@Param("app_id") String appId);
}
