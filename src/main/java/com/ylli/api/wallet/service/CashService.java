package com.ylli.api.wallet.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.auth.mapper.PasswordMapper;
import com.ylli.api.auth.model.Password;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.mapper.MchBaseMapper;
import com.ylli.api.mch.model.MchBase;
import com.ylli.api.model.base.DataList;
import com.ylli.api.sys.model.BankPayment;
import com.ylli.api.sys.model.SysChannel;
import com.ylli.api.sys.service.BankPaymentService;
import com.ylli.api.sys.service.ChannelService;
import com.ylli.api.third.pay.service.PingAnService;
import com.ylli.api.third.pay.service.WzClient;
import com.ylli.api.third.pay.service.XianFenService;
import com.ylli.api.wallet.Config;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.mapper.WzCashLogMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.CashReq;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.model.WzCashLog;
import com.ylli.api.wallet.model.WzRes;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CashService {

    @Value("${cash.charge}")
    public Integer cashCharge;

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
    MchBaseMapper userBaseMapper;

    @Autowired
    ChannelService channelService;

    @Autowired
    WzCashLogMapper wzCashLogMapper;

    @Autowired
    BankPaymentService bankPaymentService;

    @Autowired
    PingAnService pingAnService;

    @Autowired
    XianFenService xianFenService;

    public Object cashList(Long mchId, String phone, int offset, int limit) {

        PageHelper.offsetPage(offset, limit);
        Page<CashLog> page = (Page<CashLog>) cashLogMapper.cashList(mchId, phone);
        page.stream().forEach(item -> {
            MchBase base = userBaseMapper.selectByMchId(item.mchId);
            if (base != null) {
                item.mchName = base.mchName;
            }
        });
        DataList<CashLog> dataList = new DataList<>();
        dataList.offset = page.getStartRow();
        dataList.count = page.size();
        dataList.totalCount = page.getTotal();
        dataList.dataList = page;
        return dataList;
    }

    /**
     * 发起提现请求。
     * 扣除对应金额进入pending 池，
     * 若对应通道时网众支付，主动向网众发起代付请求
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

        if (req.money + cashCharge > wallet.recharge) {
            throw new AwesomeException(Config.ERROR_CASH_OUT_BOUND.format(String.format("%.2f", ((wallet.recharge - cashCharge) / 100.0))));
        }

        //记录日志
        CashLog log = new CashLog();
        modelMapper.map(req, log);
        log.state = CashLog.NEW;
        cashLogMapper.insertSelective(log);

        walletService.pendingSuc(wallet, req.money, cashCharge);

        SysChannel channel = channelService.getCurrentChannel(req.mchId);
        if (channel.code.equals("WZ")) {
            //自动发起提现请求；

            try {
                String str = wzClient.cash(log.name, log.bankcardNumber, log.openBank, log.subBank, "309394005125"
                        , String.format("%.2f", ((log.money + cashCharge) / 100.0)), "104", log.identityCard, log.reservedPhone, log.id.toString());
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

    /**
     * 手动提现
     *
     * @param cashLogId
     * @param success
     */
    @Transactional
    public void manualCash(Long cashLogId, Boolean success) {
        CashLog cashLog = cashLogMapper.selectByPrimaryKey(cashLogId);
        if (cashLog == null) {
            throw new AwesomeException(Config.ERROR_REQUEST_NOT_FOUND);
        }
        //原手动提现. 状态只有成功 与 失败。
        //加入系统代付之后，新增状态，进行中
        if (cashLog.state == CashLog.FINISH || cashLog.state == CashLog.FAILED) {
            throw new AwesomeException(Config.ERROR_CASH_HANDLED.format(CashLog.stateFormat(cashLog.state)));
        }
        if (cashLog.state == CashLog.PROCESS) {
            throw new AwesomeException(Config.ERROR_CASH_HANDING);
        }
        Wallet wallet = walletMapper.selectByPrimaryKey(cashLog.mchId);
        cashLog.type = CashLog.MANUAL;
        if (success == null || success) {
            /*if (wallet.recharge < cashLog.money + 300) {
                throw new AwesomeException(Config.ERROR_CHARGE_REQUEST);
            }*/
            cashLog.state = CashLog.FINISH;
            cashLogMapper.updateByPrimaryKeySelective(cashLog);

            walletService.cashSuc(wallet, cashLog.money);
            /*wallet.pending = wallet.pending - cashLog.money - 200;
            wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
            walletMapper.updateByPrimaryKeySelective(wallet);*/
        } else {
            cashLog.state = CashLog.FAILED;
            cashLogMapper.updateByPrimaryKeySelective(cashLog);

            walletService.cashFail(wallet, cashLog.money);
            /*wallet.pending = wallet.pending - cashLog.money - 200;
            wallet.recharge = wallet.recharge + cashLog.money + 200;
            walletMapper.updateByPrimaryKeySelective(wallet);*/
        }
    }

    /**
     * 网众 - 提现任务轮询
     *
     * @param cashLogId
     * @param success
     */
    public void successJobs(Long cashLogId, Boolean success) {
        CashLog cashLog = cashLogMapper.selectByPrimaryKey(cashLogId);
        if (cashLog == null) {
            return;
        }
        if (cashLog.state == CashLog.FINISH || cashLog.state == CashLog.FAILED) {
            //要求网众系统在250分钟之内更新提现状态 or 客服手动处理 否则置为失败处理，不在轮询状态。手动控制是否到账。
            //删除wz_cash_log
            WzCashLog log = new WzCashLog();
            log.logId = cashLogId;
            wzCashLogMapper.delete(log);
            return;
        }
        Wallet wallet = walletMapper.selectByPrimaryKey(cashLog.mchId);
        cashLog.type = CashLog.MANUAL;
        if (success == null || success) {
            cashLog.state = CashLog.FINISH;
            cashLogMapper.updateByPrimaryKeySelective(cashLog);

            walletService.cashSuc(wallet, cashLog.money);
        } else {
            cashLog.state = CashLog.FAILED;
            cashLogMapper.updateByPrimaryKeySelective(cashLog);

            walletService.cashFail(wallet, cashLog.money);
        }
    }

    public void sysCash(Long bankPayId, Long cashLogId) throws Exception {
        BankPayment payment = bankPaymentService.getBankPayment(bankPayId);
        if (payment == null) {
            throw new AwesomeException(Config.ERROR_PAYMENT_NOT_FOUND);
        }
        if (!payment.state) {
            throw new AwesomeException(Config.ERROR_PAYMENT_CLOSE);
        }
        CashLog cashLog = cashLogMapper.selectByPrimaryKey(cashLogId);
        if (cashLog == null) {
            throw new AwesomeException(Config.ERROR_REQUEST_NOT_FOUND);
        }
        if (cashLog.state != CashLog.NEW) {
            throw new AwesomeException(Config.ERROR_CASH_HANDLED.format(CashLog.stateFormat(cashLog.state)));
        }
        //发起平安代付
        if (payment.code.equals("pingAn")) {
            pingAnService.createPingAnOrder(cashLogId, cashLog.bankcardNumber, cashLog.name, cashLog.openBank, null, cashLog.money);
        } else if (payment.code.equals("xianFen")) {
            //TODO 系统代付 - 如何区分对公账户 与私人账户？
            xianFenService.createXianFenOrder(cashLogId,cashLog.money,cashLog.bankcardNumber,cashLog.name,cashLog.reservedPhone,1,1);
        } else {
            //其他

        }
    }
}
