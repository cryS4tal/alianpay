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

    /**
     * 代付充值 - 日志。
     */
    @Transactional
    public WalletLog log(Long authId, Long mchId, Integer money, Integer type, Integer status) {
        WalletLog log = new WalletLog();
        log.authId = authId;
        log.authName = Optional.ofNullable(mchBaseMapper.selectByMchId(authId)).map(i -> i.mchName).orElse("");
        log.mchId = mchId;
        log.mchName = Optional.ofNullable(mchBaseMapper.selectByMchId(mchId)).map(i -> i.mchName).orElse("");
        log.money = money;
        log.type = type;
        log.status = status;
        walletLogMapper.insertSelective(log);
        return log;
    }

    @Transactional
    public WalletLog log(Long authId, Long mchId, Integer money, Integer type, Integer status, String accountName, String accountNo, String recevieBank) {
        WalletLog log = new WalletLog();
        log.authId = authId;
        log.authName = Optional.ofNullable(mchBaseMapper.selectByMchId(authId)).map(i -> i.mchName).orElse("");
        log.mchId = mchId;
        log.mchName = Optional.ofNullable(mchBaseMapper.selectByMchId(mchId)).map(i -> i.mchName).orElse("");
        log.money = money;
        log.type = type;
        log.status = status;
        log.accountName = accountName;
        log.accountNo = accountNo;
        log.recevieBank = recevieBank;
        walletLogMapper.insertSelective(log);
        return log;
    }

    public Object getLogs(Long mchId, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<WalletLog> page = (Page<WalletLog>) walletLogMapper.selectByMchId(mchId);

        DataList<WalletLog> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }
}
