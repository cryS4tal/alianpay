package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.MchSub;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface MchSubMapper extends Mapper<MchSub> {

    List<MchSub> agencyList(@Param("type") Integer type,
                            @Param("mch_id") Long mchId,
                            @Param("sub_id") Long subId);
}
