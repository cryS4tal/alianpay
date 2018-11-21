package com.ylli.api.auth.mapper;

import com.ylli.api.auth.model.Notice;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by yqpeng on 2017/3/17.
 */
public interface NoticeMapper extends Mapper<Notice> {
    List<Notice> getList(@Param("owner_id") Long ownerId,
                         @Param("state") Integer state,
                         @Param("type") String type);

    @Update("update t_notice set state = 1,modify_time = now() where id = ${id}")
    void markRead(@Param("id") Long id);
}
