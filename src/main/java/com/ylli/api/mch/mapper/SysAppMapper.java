package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.SysApp;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SysAppMapper extends Mapper<SysApp> {
    List<SysApp> getSysApp(@Param("app_name") String appName,
                           @Param("status") Boolean status);

    @Select("SELECT * FROM t_sys_app WHERE code = #{code}")
    SysApp selectByCode(@Param("code") String payType);
}
