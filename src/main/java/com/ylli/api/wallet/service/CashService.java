package com.ylli.api.wallet.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.auth.mapper.PasswordMapper;
import com.ylli.api.auth.model.Password;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.model.base.DataList;
import com.ylli.api.pay.model.SysChannel;
import com.ylli.api.pay.service.ChannelService;
import com.ylli.api.wallet.Config;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.mapper.WzCashLogMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.CashReq;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.model.WzCashLog;
import com.ylli.api.wallet.model.WzRes;
import com.ylli.api.wzpay.service.WzClient;
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
    WalletService walletService;

    @Autowired
    WalletMapper walletMapper;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    WzClient wzClient;

    @Autowired
    ChannelService channelService;

    @Autowired
    WzCashLogMapper wzCashLogMapper;

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

        if (req.money + 200 > wallet.recharge) {
            throw new AwesomeException(Config.ERROR_CASH_OUT_BOUND.format(String.format("%.2f", ((wallet.recharge - 200) / 100.0))));
        }

        //记录日志
        CashLog log = new CashLog();
        modelMapper.map(req, log);
        log.state = CashLog.NEW;
        cashLogMapper.insertSelective(log);

        wallet.recharge = wallet.recharge - req.money - 200;
        wallet.pending = wallet.pending + req.money + 200;
        walletMapper.updateByPrimaryKeySelective(wallet);

        SysChannel channel = channelService.getCurrentChannel();
        if (channel.code.equals("WZ")) {
            //自动发起提现请求；

            try {
                String str = wzClient.cash(log.name, log.bankcardNumber, log.openBank, log.subBank, "309394005125"
                        , String.format("%.2f", (log.money / 100.0)), "104", log.identityCard, log.reservedPhone, log.id.toString());
                WzRes res = new Gson().fromJson(str, WzRes.class);
                if (!res.code.equals("200")) {
                    throw new AwesomeException(Config.ERROR_REQUEST_FAIL.format(res.msg));
                } else {
                    WzCashLog wzCashLog = new WzCashLog();
                    wzCashLog.logId = log.id;
                    wzCashLogMapper.insertSelective(wzCashLog);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Transactional
    public void success(Long cashLogId, Boolean success) {
        CashLog cashLog = cashLogMapper.selectByPrimaryKey(cashLogId);
        if (cashLog == null) {
            throw new AwesomeException(Config.ERROR_REQUEST_NOT_FOUND);
        }
        if (cashLog.state == CashLog.FINISH || cashLog.state == CashLog.FAILED) {
            throw new AwesomeException(Config.ERROR_CASH_HANDLED.format(CashLog.stateFormat(cashLog.state)));
        }
        Wallet wallet = walletMapper.selectByPrimaryKey(cashLog.mchId);
        if (success == null || success) {
            if (wallet.recharge < cashLog.money + 200) {
                throw new AwesomeException(com.ylli.api.user.Config.ERROR_CHARGE_REQUEST);
            }
            cashLog.state = CashLog.FINISH;
            cashLogMapper.updateByPrimaryKeySelective(cashLog);

            wallet.pending = wallet.pending - cashLog.money - 200;
            wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
            walletMapper.updateByPrimaryKeySelective(wallet);
        } else {
            cashLog.state = CashLog.FAILED;
            cashLogMapper.updateByPrimaryKeySelective(cashLog);

            wallet.pending = wallet.pending - cashLog.money - 200;
            wallet.recharge = wallet.recharge + cashLog.money + 200;
            walletMapper.updateByPrimaryKeySelective(wallet);
        }
    }
}
