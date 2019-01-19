package com.ylli.api.third.pay.mapper;

import com.ylli.api.third.pay.model.QrPendInfo;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface QrPendInfoMapper extends Mapper<QrPendInfo> {

    List<QrPendInfo> getPending(@Param("name") String name,
                                @Param("money") Integer money,
                                @Param("start_time") Date startTime,
                                @Param("end_time") Date endTime);
}
