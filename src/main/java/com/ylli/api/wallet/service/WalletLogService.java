package com.ylli.api.wallet.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.model.base.DataList;
import com.ylli.api.wallet.mapper.WalletLogMapper;
import com.ylli.api.wallet.model.WalletLog;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletLogService {

    @Autowired
    WalletLogMapper walletLogMapper;

    @Autowired
    MchBaseMapper mchBaseMapper;

    @Transactional
    public void log(Long authId, Long mchId, Integer money, Integer type) {
        WalletLog log = new WalletLog();
        log.authId = authId;
        log.authName = Optional.ofNullable(mchBaseMapper.selectByMchId(authId)).map(i -> i.mchName).orElse("");
        log.mchId = mchId;
        log.mchName = Optional.ofNullable(mchBaseMapper.selectByMchId(mchId)).map(i -> i.mchName).orElse("");
        log.money = money;
        log.type = type;
        walletLogMapper.insertSelective(log);
    }

    public Object getLogs(Long mchId, int offset, int limit) {

        WalletLog mchLog = new WalletLog();
        mchLog.mchId = mchId;

        PageHelper.offsetPage(offset, limit);
        Page<WalletLog> page = (Page<WalletLog>) walletLogMapper.select(mchLog);

        DataList<WalletLog> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }
}
