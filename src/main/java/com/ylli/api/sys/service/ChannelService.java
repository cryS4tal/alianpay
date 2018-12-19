package com.ylli.api.sys.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.sys.Config;
import com.ylli.api.sys.mapper.MchChannelMapper;
import com.ylli.api.sys.mapper.SysChannelMapper;
import com.ylli.api.sys.model.MchChannel;
import com.ylli.api.sys.model.SysChannel;
import java.util.List;
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

    @Autowired
    AccountMapper accountMapper;

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


    public Object sysChannels(int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<SysChannel> page = (Page<SysChannel>) sysChannelMapper.selectAll();

        DataList<SysChannel> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    public Object mchChannels(Long mchId, String mchName, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        List<Account> accounts = accountMapper.selectByCondition(mchId, mchName);




        return null;
    }
}
