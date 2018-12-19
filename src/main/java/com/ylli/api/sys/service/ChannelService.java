package com.ylli.api.sys.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.sys.Config;
import com.ylli.api.sys.mapper.MchChannelMapper;
import com.ylli.api.sys.mapper.SysChannelMapper;
import com.ylli.api.sys.model.MchChannel;
import com.ylli.api.sys.model.SysChannel;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService {

    @Autowired
    SysChannelMapper sysChannelMapper;

    @Autowired
    MchChannelMapper mchChannelMapper;

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
     * v1.1 每个用户可以获取自己的通道。
     *
     * @return
     */
    public SysChannel getCurrentChannel(Long mchId) {

        MchChannel mchChannel = new MchChannel();
        mchChannel.mchId = mchId;
        mchChannel = mchChannelMapper.selectOne(mchChannel);
        if (mchChannel == null) {
            //默认插入 网众。后续再改
            mchChannel = channelInit(mchId, 2L);
        }
        SysChannel channel = sysChannelMapper.selectByPrimaryKey(mchChannel.channelId);
        return channel;
    }

    @Transactional
    public void mchChannelSwitch(Long mchId, Long channelId) {
        MchChannel mchChannel = new MchChannel();
        mchChannel.mchId = mchId;
        mchChannel = mchChannelMapper.selectOne(mchChannel);
        if (mchChannel == null) {
            mchChannel = channelInit(mchId, channelId);
        } else {
            mchChannel.channelId = channelId;
            mchChannelMapper.updateByPrimaryKeySelective(mchChannel);
        }
    }


    @Transactional
    public MchChannel channelInit(Long mchId, Long channelId) {
        MchChannel mchChannel = new MchChannel();
        mchChannel.mchId = mchId;
        mchChannel.channelId = channelId;
        mchChannelMapper.insertSelective(mchChannel);
        return mchChannel;
    }

    public String getChannelName(Long channelId) {
        SysChannel sysChannel = sysChannelMapper.selectByPrimaryKey(channelId);
        return Optional.ofNullable(sysChannel).map(i -> i.name).orElse(null);
    }
}
