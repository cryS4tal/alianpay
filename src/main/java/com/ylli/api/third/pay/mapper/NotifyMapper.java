package com.ylli.api.third.pay.mapper;

import com.ylli.api.third.pay.model.Notify;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface NotifyMapper extends Mapper<Notify> {

    @Select("SELECT * FROM t_notify WHERE TIMESTAMPDIFF(SECOND,modify_time,NOW()) >= 60;")
    List<Notify> selectNotify();
}
