package com.ylli.api.user.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.ylli.api.auth.mapper.AccountPasswordMapper;
import com.ylli.api.auth.model.AccountPassword;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.user.Config;
import com.ylli.api.user.mapper.CashLogMapper;
import com.ylli.api.user.mapper.UserSettlementMapper;
import com.ylli.api.user.model.CashLog;
import com.ylli.api.user.model.UserChargeInfo;
import com.ylli.api.user.model.UserOwnInfo;
import com.ylli.api.user.model.UserSettlement;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSettlementService {

    @Autowired
    UserSettlementMapper userSettlementMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AccountPasswordMapper accountPasswordMapper;

    @Autowired
    BillService billService;

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    WalletService walletService;

    @Transactional
    public Object saveUserInfo(UserOwnInfo ownInfo) {
        UserSettlement settlement = userSettlementMapper.selectByUserId(ownInfo.userId);
        if (settlement == null) {
            settlement = new UserSettlement();
            modelMapper.map(ownInfo, settlement);
            userSettlementMapper.insertSelective(settlement);
        } else {
            modelMapper.map(ownInfo, settlement);
            settlement.modifyTime = Timestamp.from(Instant.now());
            userSettlementMapper.updateByPrimaryKeySelective(settlement);
        }
        return userSettlementMapper.selectByPrimaryKey(settlement.id);
    }


    @Transactional
    public Object saveChargeInfo(UserChargeInfo userChargeInfo) {
        UserSettlement settlement = userSettlementMapper.selectByUserId(userChargeInfo.userId);
        if (settlement == null) {
            throw new AwesomeException(Config.ERROR_USER_NOT_FOUND);
        }
        settlement.chargeType = userChargeInfo.chargeType;
        settlement.chargeRate = userChargeInfo.chargeRate;
        settlement.modifyTime = Timestamp.from(Instant.now());
        userSettlementMapper.updateByPrimaryKeySelective(settlement);

        walletService.automaticBonus(userChargeInfo.userId, userChargeInfo.chargeType, userChargeInfo.chargeRate);

        return userSettlementMapper.selectByPrimaryKey(settlement.id);
    }

    public Object getUserList(Long userId, String name, String identityCard, String bankcardNumber,
                              String reservedPhone, String openBank, String subBank, Timestamp beginTime,
                              Timestamp endTime, int offset, int limit) {
        PageHelper.offsetPage(offset, limit);
        Page<UserSettlement> page = (Page<UserSettlement>) userSettlementMapper.selectByCondition(userId, name, identityCard,
                bankcardNumber, reservedPhone, openBank, subBank, beginTime, endTime);
        DataList<UserSettlement> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    public UserSettlement getUserInfo(Long userId) {
        return userSettlementMapper.selectByUserId(userId);
    }

    @Transactional
    public void removeUserInfo(long id) {
        userSettlementMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public void cash(Long userId, Integer money, String password) {
        AccountPassword accountPassword = accountPasswordMapper.selectByPrimaryKey(userId);
        if (Strings.isNullOrEmpty(password) || !BCrypt.checkpw(password, accountPassword.password)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
        UserSettlement settlement = userSettlementMapper.selectByUserId(userId);
        if (settlement == null) {
            throw new AwesomeException(Config.ERROR_SETTLEMENT_EMPTY);
        }
        if (settlement.chargeType == null || settlement.chargeRate == null) {
            throw new AwesomeException(Config.ERROR_SETTLEMENT_CHARGE_EMPTY);
        }
        //金额计算.
        Wallet wallet = walletService.getOwnWallet(userId);

        Integer already = doSome(userId);
        if (money > wallet.recharge + wallet.bonus - already) {
            throw new AwesomeException(Config.ERROR_CASH_OUT_BOUND.format(String.format("%.2f", (wallet.recharge + wallet.bonus - already) / 100.0)));
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

        //去wallet 分润金额减去 金额。  更新相应的  bonus  total. (暂时不考虑手续费？)
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
}
