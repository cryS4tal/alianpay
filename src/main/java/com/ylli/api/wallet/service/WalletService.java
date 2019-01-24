package com.ylli.api.wallet.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.mch.model.MchAgency;
import com.ylli.api.mch.service.MchAgencyService;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.wallet.Config;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.model.WalletLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    @Value("${cash.charge}")
    public Integer cashCharge;

    @Value("${bank.pay.pwd}")
    public String sysPwd;

    @Autowired
    WalletMapper walletMapper;

    @Autowired
    MchAgencyService mchAgencyService;

    @Autowired
    WalletLogService walletLogService;

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

    /*@Transactional
    public Object conversion(Long mchId, Integer money) {
        Wallet wallet = getOwnWallet(mchId);
        if (wallet.recharge < money) {
            throw new AwesomeException(Config.ERROR_WALLET_CONVERSION.format(String.format("%.2f", wallet.recharge / 100.0)));
        }
        wallet.recharge = wallet.recharge - money;
        wallet.reservoir = wallet.reservoir + money;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
        return wallet;
    }*/

    /**
     * 充值代付池
     */
    @Transactional
    public Object recharge(Long authId, Long mchId, Integer money, String password) {
        if (!password.equals(sysPwd)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
        Wallet wallet = getOwnWallet(mchId);
        wallet.reservoir = wallet.reservoir + money;
        walletMapper.updateByPrimaryKeySelective(wallet);
        walletLogService.log(authId, mchId, money, WalletLog.XTCZ);
        return wallet;
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
}
