package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.MchAgency;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface MchAgencyMapper extends Mapper<MchAgency> {

    List<MchAgency> agencyList(@Param("type") Integer type,
                               @Param("mch_id") Long mchId,
                               @Param("sub_id") Long subId);
}
