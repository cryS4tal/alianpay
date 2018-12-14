package com.ylli.api.sys.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.sys.Config;
import com.ylli.api.sys.mapper.SysChannelMapper;
import com.ylli.api.sys.model.SysChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService {

    @Autowired
    SysChannelMapper sysChannelMapper;

    @Transactional
    public void channelSwitch(Long id, Boolean isOpen) {
        SysChannel sysChannel = sysChannelMapper.selectByPrimaryKey(id);
        if (sysChannel == null) {
            throw new AwesomeException(Config.ERROR_CHANNEL_NOT_FOUND);
        }
        sysChannel.state = isOpen;
        sysChannelMapper.updateByPrimaryKeySelective(sysChannel);
    }

    /**
     * 获得当前通道 v1.0
     *
     * @return
     */
    public SysChannel getCurrentChannel() {
        SysChannel channel = new SysChannel();
        channel.state = true;
        channel = sysChannelMapper.selectOne(channel);
        return channel;
    }
}
