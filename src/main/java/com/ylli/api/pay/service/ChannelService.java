package com.ylli.api.pay.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.pay.Config;
import com.ylli.api.pay.mapper.SysChannelMapper;
import com.ylli.api.pay.model.SysChannel;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService {

    @Autowired
    SysChannelMapper sysChannelMapper;

    @Transactional
    public void channelSwitch(Long id) {
        SysChannel sysChannel = sysChannelMapper.selectByPrimaryKey(id);
        if (sysChannel == null) {
            throw new AwesomeException(Config.ERROR_CHANNEL_NOT_FOUND);
        }
        // v1.0暂时只支持单通道
        List<SysChannel> list = sysChannelMapper.selectAll();
        list.stream().forEach(i -> {
            i.state = false;
            sysChannelMapper.updateByPrimaryKeySelective(i);
        });
        sysChannel.state = true;
        sysChannelMapper.updateByPrimaryKeySelective(sysChannel);
    }

    /**
     * 获得当前通道 v1.0
     * @return
     */
    public SysChannel getCurrentChannel() {
        SysChannel channel = new SysChannel();
        channel.state = true;
        channel = sysChannelMapper.selectOne(channel);
        return channel;
    }
}
