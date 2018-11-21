package com.ylli.api.auth.mapper;

import com.ylli.api.auth.model.Department;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

/**
 * Created by yqpeng on 2017/2/20.
 */
public interface DepartmentMapper extends Mapper<Department> {
    List<Department> getDeptList(@Param("name_like") String nameLike);
}
