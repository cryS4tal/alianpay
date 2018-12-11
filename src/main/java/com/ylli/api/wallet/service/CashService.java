package com.ylli.api.wallet.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.auth.mapper.PasswordMapper;
import com.ylli.api.auth.mapper.PhoneAuthMapper;
import com.ylli.api.auth.model.Password;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.user.mapper.UserSettlementMapper;
import com.ylli.api.wallet.Config;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.CashReq;
import com.ylli.api.wallet.model.Wallet;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashService {

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    PasswordMapper passwordMapper;

    @Autowired
    UserSettlementMapper settlementMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    WalletMapper walletMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PhoneAuthMapper phoneAuthMapper;

    public Object cashList(Long mchId, String phone, int offset, int limit) {

        PageHelper.offsetPage(offset, limit);
        Page<CashLog> page = (Page<CashLog>) cashLogMapper.cashList(mchId, phone);
        DataList<CashLog> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    /**
     * 后续接入api...
     */
    @Transactional
    public void cash(CashReq req) {

        Password password = passwordMapper.selectByPrimaryKey(req.mchId);
        if (Strings.isNullOrEmpty(req.password) || !BCrypt.checkpw(req.password, password.password)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
        // 金额计算.
        // v1.0 加入限制，存在待处理的体现申请，金额进入钱包待处理部分
        Wallet wallet = walletService.getOwnWallet(req.mchId);

        /*CashLog cashLog = new CashLog();
        cashLog.mchId = req.mchId;
        cashLog.state = CashLog.NEW;
        List<CashLog> logs = cashLogMapper.select(cashLog);
        int pending = 0;
        for (int i = 0; i < logs.size(); i++) {
            pending = pending + logs.get(i).money;
        }*/

        if (req.money + 200 > wallet.recharge) {
            throw new AwesomeException(Config.ERROR_CASH_OUT_BOUND.format(String.format("%.2f", ((wallet.recharge - 200) / 100.0))));
        }

        //记录日志
        CashLog log = new CashLog();
        modelMapper.map(req, log);
        log.state = CashLog.NEW;
        cashLogMapper.insertSelective(log);

        wallet.recharge = wallet.recharge - req.money;
        wallet.pending = wallet.pending + req.money;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    @Transactional
    public void success(Long cashLogId) {
        CashLog cashLog = cashLogMapper.selectByPrimaryKey(cashLogId);
        if (cashLog == null) {
            throw new AwesomeException(Config.ERROR_REQUEST_NOT_FOUND);
        }
        /*if (cashLog.isOk) {
            throw new AwesomeException(Config.ERROR_CASH_HANDLED);
        }*/
        /*Wallet wallet = walletMapper.selectByPrimaryKey(cashLog.userId);
        if (wallet.recharge < cashLog.money + 200) {
            throw new AwesomeException(com.ylli.api.user.Config.ERROR_CHARGE_REQUEST);
        }*/
        //cashLog.isOk = true;
        cashLogMapper.updateByPrimaryKeySelective(cashLog);

        //wallet.recharge = wallet.recharge - cashLog.money - 200;
        //wallet.total = wallet.recharge + wallet.bonus;
        //walletMapper.updateByPrimaryKeySelective(wallet);
    }
}
