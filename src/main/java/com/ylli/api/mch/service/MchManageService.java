package com.ylli.api.mch.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.Config;
import com.ylli.api.mch.model.Mch;
import com.ylli.api.model.base.DataList;
import com.ylli.api.sys.model.SysChannel;
import com.ylli.api.sys.service.ChannelService;
import com.ylli.api.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MchManageService {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    ChannelService channelService;

    public Object mchList(String phone, String mchId, String mchName, Integer auditState, String mchState, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<Mch> page = (Page<Mch>) accountMapper.selectByQuery(phone, mchId, mchName, auditState, mchState);
        DataList<Mch> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        page.stream().forEach(item -> {
            item.money = walletService.getOwnWallet(item.mchId).total;
            SysChannel channel = channelService.getCurrentChannel(item.mchId);
            item.channelId = channel.id;
            item.channelName = channel.name;
        });
        dataList.dataList = page;
        return dataList;
    }

    @Transactional
    public void mchEnable(Long mchId, Boolean open) {
        Account account = accountMapper.selectByPrimaryKey(mchId);
        if (account == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        account.state = open ? Account.STATE_ENABLE : Account.STATE_DISABLE;
        accountMapper.updateByPrimaryKeySelective(account);
    }
}
