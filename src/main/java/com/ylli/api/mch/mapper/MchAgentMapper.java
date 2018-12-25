package com.ylli.api.mch.mapper;

import com.ylli.api.mch.model.MchAgent;
import com.ylli.api.mch.model.MchAgentDto;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MchAgentMapper extends Mapper<MchAgent> {
    List<MchAgentDto> getAgents();
}
