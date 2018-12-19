package com.ylli.api.pay.mapper;

import com.ylli.api.pay.model.AsyncMessage;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface AsyncMessageMapper extends Mapper<AsyncMessage> {
    @Select("SELECT * FROM t_async_message where fail_count < ${limit}")
    List<AsyncMessage> selectInLimit(@Param("limit") Integer limit);
}
