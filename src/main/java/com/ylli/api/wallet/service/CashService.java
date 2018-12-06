package com.ylli.api.wallet.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.auth.mapper.AccountPasswordMapper;
import com.ylli.api.auth.model.AccountPassword;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.user.mapper.UserSettlementMapper;
import com.ylli.api.user.model.UserSettlement;
import com.ylli.api.wallet.Config;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.CashLogDetail;
import com.ylli.api.wallet.model.Wallet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashService {

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    AccountPasswordMapper passwordMapper;

    @Autowired
    UserSettlementMapper settlementMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    WalletMapper walletMapper;

    public Object cashList(Long mchId, String phone, int offset, int limit) {

        PageHelper.offsetPage(offset, limit);
        Page<CashLog> page = (Page<CashLog>) cashLogMapper.cashList(mchId, phone);
        DataList<CashLogDetail> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page.stream().map(item -> detailConvert(item, phone)).collect(Collectors.toList());
        return dataList;
    }

    public CashLogDetail detailConvert(CashLog cashLog, String phone) {
        return new CashLogDetail(cashLog.userId, cashLog.money, cashLog.isOk, phone);
    }

    /**
     * 后续接入api...
     *
     * @param userId
     * @param money
     * @param password
     */
    @Transactional
    public void cash(Long userId, Integer money, String password) {
        AccountPassword accountPassword = passwordMapper.selectByPrimaryKey(userId);
        if (Strings.isNullOrEmpty(password) || !BCrypt.checkpw(password, accountPassword.password)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
        UserSettlement settlement = settlementMapper.selectByUserId(userId);
        if (settlement == null) {
            throw new AwesomeException(Config.ERROR_SETTLEMENT_EMPTY);
        }
        if (settlement.chargeType == null || settlement.chargeRate == null) {
            throw new AwesomeException(Config.ERROR_SETTLEMENT_CHARGE_EMPTY);
        }
        //金额计算.
        Wallet wallet = walletService.getOwnWallet(userId);

        Integer already = doSome(userId);
        if (money + 200 > wallet.recharge - already) {
            throw new AwesomeException(Config.ERROR_CASH_OUT_BOUND.format(String.format("%.2f", (wallet.recharge - 200 - already) / 100.0)));
        }
        //记录日志
        CashLog log = new CashLog();
        //暂时code记录用户id；msg = 金额
        log.userId = userId;
        log.money = money;
        log.isOk = false;
        cashLogMapper.insertSelective(log);

        //接收到用户提现请求之后。。
        //先去 cash_log... 获得需要提现的总金额。

        //去wallet  recharge 可用余额。  更新相应的  recharge  total. （- 2 元手续费）
        //回头更新 cash_log 对应的提现请求  is_ok = true.

    }

    //结算金额计算
    public Integer doSome(Long userId) {
        CashLog log = new CashLog();
        log.userId = userId;
        log.isOk = true;
        List<CashLog> logs = cashLogMapper.select(log);
        if (logs.size() == 0) {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < logs.size(); i++) {
            sum = sum + logs.get(i).money;
        }
        return sum;
    }

    @Transactional
    public void success(Long cashLogId) {
        CashLog cashLog = cashLogMapper.selectByPrimaryKey(cashLogId);
        if (cashLog == null) {
            throw new AwesomeException(Config.ERROR_REQUEST_NOT_FOUND);
        }
        if (cashLog.isOk) {
            throw new AwesomeException(Config.ERROR_CASH_HANDLED);
        }
        Wallet wallet = walletMapper.selectByPrimaryKey(cashLog.userId);
        if (wallet.recharge < cashLog.money + 200) {
            throw new AwesomeException(com.ylli.api.user.Config.ERROR_CHARGE_REQUEST);
        }
        cashLog.isOk = true;
        cashLogMapper.updateByPrimaryKeySelective(cashLog);

        wallet.recharge = wallet.recharge - cashLog.money - 200;
        wallet.total = wallet.recharge + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }
}
