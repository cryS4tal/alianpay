package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.MchSub;
import com.ylli.api.mch.model.MchSubDto;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MchSubMapper extends Mapper<MchSub> {
    List<MchSubDto> selectSubAccounts(@Param("mch_id") Long mchId);
}
