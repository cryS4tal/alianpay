package com.ylli.api.wallet.service;

import com.google.gson.Gson;
import com.ucf.sdk.UcfForOnline;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.model.MchAgency;
import com.ylli.api.mch.service.MchAgencyService;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.third.pay.modelVo.xianfen.Data;
import com.ylli.api.third.pay.modelVo.xianfen.XianFenResponse;
import com.ylli.api.third.pay.service.xianfen.XfClient;
import com.ylli.api.wallet.Config;
import com.ylli.api.wallet.mapper.WalletLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.model.WalletLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private static Logger LOGGER = LoggerFactory.getLogger(WalletService.class);

    @Value("${cash.charge}")
    public Integer cashCharge;

    @Value("${bank.pay.pwd}")
    public String sysPwd;

    @Value("${xf.pay.mer_pri_key}")
    public String mer_pri_key;

    @Autowired
    WalletMapper walletMapper;

    @Autowired
    MchAgencyService mchAgencyService;

    @Autowired
    WalletLogService walletLogService;

    @Autowired
    WalletLogMapper walletLogMapper;

    @Autowired
    XfClient xfClient;

    public Wallet getOwnWallet(Long mchId) {
        return walletMapper.selectByPrimaryKey(mchId);
    }

    @Transactional
    public void init(Long id) {
        Wallet wallet = new Wallet();
        wallet.id = id;
        walletMapper.insertSelective(wallet);
    }

    /**
     * 支付成功。钱包余额到账
     *
     * @param mchId       商户id，对于有代理商的商户会计算代理商的分润
     * @param orderMoney  下单金额
     * @param chargeMoney 手续费
     * @param payType     支付类型
     */
    @Transactional
    public void incr(Long mchId, Integer orderMoney, Integer chargeMoney, String payType) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.recharge = wallet.recharge + orderMoney - chargeMoney;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);

        //加入分润计算.
        MchAgency sup = mchAgencyService.getPaySupper(mchId);
        if (sup != null) {
            Wallet wallet1 = walletMapper.selectByPrimaryKey(sup.mchId);
            if (PayService.ALI.equals(payType)) {
                wallet1.bonus = wallet1.bonus + orderMoney * sup.alipayRate / 10000;
            } else if (PayService.WX.equals(payType)) {
                wallet1.bonus = wallet1.bonus + orderMoney * sup.wxRate / 10000;
            } else {
                //预留其他情况.

            }
            wallet1.total = wallet1.recharge + wallet1.pending + wallet1.bonus;
            walletMapper.updateByPrimaryKeySelective(wallet1);
        }
    }

    @Transactional
    public void cashSuc(Wallet wallet, Integer money, Integer cashCharge) {
        wallet.pending = wallet.pending - money - cashCharge;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;

        //手续费0标识为商户发起代付池转换请求，success，代付池余额增加
        //@see recharge() method
        if (0 == cashCharge.intValue()) {
            wallet.reservoir = wallet.reservoir + money;
        }
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    /**
     * 商户发起提现请求，走系统代付（弃用）success
     * 保留 for 平安代付，
     * cnt相关可删，暂存
     */
    @Transactional
    @Deprecated
    public void cashSuc(Long mchId, Integer money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        cashSuc(wallet, money, cashCharge);
    }

    @Transactional
    public void cashFail(Wallet wallet, Integer money, Integer cashCharge) {
        wallet.pending = wallet.pending - money - cashCharge;

        //分润金额提现失败暂时回滚至交易金额。
        wallet.recharge = wallet.recharge + money + cashCharge;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    /**
     * 商户发起提现请求，走系统代付（弃用）fail
     */
    @Transactional
    @Deprecated
    public void cashFail(Long mchId, Integer money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        cashFail(wallet, money, cashCharge);
    }

    /**
     * 补单金额回滚
     */
    @Transactional
    public void rollback(Long mchId, Integer orderMoney, Integer chargeMoney, String payType) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.recharge = wallet.recharge - orderMoney + chargeMoney;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);

        //分润金额回滚.
        MchAgency sup = mchAgencyService.getPaySupper(mchId);
        if (sup != null) {
            Wallet wallet1 = walletMapper.selectByPrimaryKey(sup.mchId);
            if (PayService.ALI.equals(payType)) {
                wallet1.bonus = wallet1.bonus - orderMoney * sup.alipayRate / 10000;
            } else if (PayService.WX.equals(payType)) {
                wallet1.bonus = wallet1.bonus - orderMoney * sup.wxRate / 10000;
            } else {
                //预留其他情况.

            }
            wallet1.total = wallet1.recharge + wallet1.pending + wallet1.bonus;
            walletMapper.updateByPrimaryKeySelective(wallet1);
        }

    }


    /**
     * 用户发起提现请求。金额进入在途.
     *
     * @param wallet
     * @param money
     * @param cashCharge
     */
    @Transactional
    public void pendingSuc(Wallet wallet, Integer money, Integer cashCharge) {
        Integer all = money + cashCharge;

        //优先减去交易金额.
        if (all < wallet.recharge) {
            wallet.recharge = wallet.recharge - all;
        } else {
            wallet.bonus = wallet.bonus - (all - wallet.recharge);
            wallet.recharge = 0;
        }
        wallet.pending = wallet.pending + all;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    /**
     * 商户接口发起代付请求，扣除代付池中可用余额 （请求金额 + 手续费）
     *
     * @param mchId
     * @param money 请求金额 + 手续费
     */
    @Transactional
    public void decrReservoir(Long mchId, Integer money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.reservoir = wallet.reservoir - money;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    /**
     * 商户接口发起代付请求，回滚金额。
     *
     * @param mchId
     * @param money
     */
    @Transactional
    public void incrReservoir(Long mchId, Integer money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.reservoir = wallet.reservoir + money;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    /**
     * 充值代付池 - 管理员
     */
    @Transactional
    public Object recharge(Long authId, Long mchId, Integer money, String password) {
        if (!password.equals(sysPwd)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
        Wallet wallet = getOwnWallet(mchId);
        wallet.reservoir = wallet.reservoir + money;
        walletMapper.updateByPrimaryKeySelective(wallet);
        walletLogService.log(authId, mchId, money, WalletLog.XXCZ, WalletLog.FINISH);
        return wallet;
    }

    /**
     * 充值代付池 - 商户
     */
    @Transactional
    public Object rechargeMch(Long mchId, Integer money, String accountName, String accountNo, String recevieBank) throws Exception {
        //参数合法性校验。
        //money - 范围
        if (money <= 10 * 100) {
            throw new AwesomeException(Config.ERROR.format("单笔充值金额不得低于10元"));
        }
        //accountName & accountNo 姓名和卡号暂时不进行校验
        //recevieBank UPOPJS（银联）NUCC（网联）
        if (!"UPOPJS".equals(recevieBank) && !"NUCC".equals(recevieBank)) {
            throw new AwesomeException(Config.ERROR_RECRIVE_BANK_ERROR);
        }
        WalletLog walletLog = walletLogService.log(mchId, mchId, money, WalletLog.XXCZ, WalletLog.ING, accountName, accountNo, recevieBank);
        String str = xfClient.offlineRecharge(walletLog.id.toString(), money, accountNo, accountName, recevieBank, mchId.toString());

        LOGGER.info("\n xianFen mch recharge offline response. \n " + str);
        //
        XianFenResponse response = new Gson().fromJson(str, XianFenResponse.class);
        if (("99000").equals(response.code)) {
            //加密后的业务数据
            String bizData = UcfForOnline.decryptData(str, mer_pri_key);

            //
            Data data = new Gson().fromJson(bizData, Data.class);

            LOGGER.info("\n xianFen mch recharge offline response bizData. \n " + bizData);
            if (data.resCode.equals("00000")) {
                if (data.status.equals("S")) {
                    //异步回调success逻辑

                    Wallet wallet = getOwnWallet(mchId);
                    wallet.reservoir = wallet.reservoir + money;
                    walletMapper.updateByPrimaryKeySelective(wallet);

                    walletLog.status = WalletLog.FINISH;
                    walletLogMapper.updateByPrimaryKeySelective(walletLog);
                }
                if (data.status.equals("F")) {
                    //异步回调fail逻辑

                    walletLog.status = WalletLog.FAIL;
                    walletLogMapper.updateByPrimaryKeySelective(walletLog);
                }
                if (data.status.equals("I")) {
                    //不处理
                }
            } else {
                throw new AwesomeException(Config.ERROR.format(new StringBuffer(data.resCode).append("|").append(data.resMessage)));
            }
            return bizData;
        } else {
            throw new AwesomeException(Config.ERROR.format(new StringBuffer(response.code).append("|").append(response.message)));
        }
    }

    /**
     * 代付分润计算.
     *
     * @param mchId
     * @param money
     */
    @Transactional
    public void incrBankBonus(Long mchId, Integer money) {
        MchAgency sup = mchAgencyService.getBankSupper(mchId);
        if (sup != null) {
            Wallet wallet = walletMapper.selectByPrimaryKey(sup.mchId);
            wallet.bonus = wallet.bonus + money * sup.bankRate / 10000;
            wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
            walletMapper.updateByPrimaryKeySelective(wallet);
        }
    }

    @Transactional
    public Object rechargeQuery(long authId, String id) throws Exception {
        WalletLog walletLog = walletLogMapper.selectByPrimaryKey(Long.parseLong(id));
        if (walletLog == null) {
            throw new AwesomeException(Config.ERROR_REQUEST_NOT_FOUND);
        }
        if (walletLog.mchId != authId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        if (walletLog.status != WalletLog.ING) {
            return walletLog;
        }

        String str = xfClient.rechargeQuery(id);

        LOGGER.info("\n xianFen mch recharge offline query response. \n " + str);

        XianFenResponse response = new Gson().fromJson(str, XianFenResponse.class);
        if (("99000").equals(response.code)) {
            //加密后的业务数据
            String bizData = UcfForOnline.decryptData(str, mer_pri_key);

            LOGGER.info("\n xianFen mch recharge offline query response bizData. \n " + bizData);
            //
            Data data = new Gson().fromJson(bizData, Data.class);
            if (data.resCode.equals("00000")) {
                if (data.status.equals("S")) {
                    //异步回调success逻辑

                    Wallet wallet = getOwnWallet(authId);
                    wallet.reservoir = wallet.reservoir + walletLog.money;
                    walletMapper.updateByPrimaryKeySelective(wallet);

                    walletLog.status = WalletLog.FINISH;
                    walletLogMapper.updateByPrimaryKeySelective(walletLog);
                }
                if (data.status.equals("F")) {
                    //异步回调fail逻辑

                    walletLog.status = WalletLog.FAIL;
                    walletLogMapper.updateByPrimaryKeySelective(walletLog);
                }
                if (data.status.equals("I")) {
                    //不处理
                }
                return walletLog;
            } else {
                throw new AwesomeException(Config.ERROR.format(new StringBuffer(data.resCode).append("|").append(data.resMessage)));
            }
        } else {
            throw new AwesomeException(Config.ERROR.format(new StringBuffer(response.code).append("|").append(response.message)));
        }
    }
}
