package com.ylli.api.sys.mapper;

import com.ylli.api.sys.model.SysChannel;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SysChannelMapper extends Mapper<SysChannel> {

    @Select("SELECT * FROM t_sys_channel ORDER BY state DESC")
    List<SysChannel> selectAllOrderByState();
}
