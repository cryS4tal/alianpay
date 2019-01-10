package com.ylli.api.wallet.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.third.pay.mapper.YfbBillMapper;
import com.ylli.api.wallet.Config;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.Wallet;
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
    YfbBillMapper yfbBillMapper;

    public Wallet getOwnWallet(Long mchId) {
        return walletMapper.selectByPrimaryKey(mchId);
    }

    @Transactional
    public void init(Long id) {
        Wallet wallet = new Wallet();
        wallet.id = id;
        walletMapper.insertSelective(wallet);
    }

    @Transactional
    public void incr(Long mchId, int money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.recharge = wallet.recharge + money;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
        // todo 加入钱包金额变动 关联 账单日志表。  wallet log
    }

    @Transactional
    public void cashSuc(Wallet wallet, Integer money) {
        wallet.pending = wallet.pending - money - cashCharge;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    @Transactional
    public void cashSuc(Long mchId, Integer money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.pending = wallet.pending - money - cashCharge;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    @Transactional
    public void cashFail(Wallet wallet, Integer money) {
        wallet.pending = wallet.pending - money - cashCharge;
        wallet.recharge = wallet.recharge + money + cashCharge;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    @Transactional
    public void cashFail(Long mchId, Integer money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.pending = wallet.pending - money - cashCharge;
        wallet.recharge = wallet.recharge + money + cashCharge;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    /**
     * 补单金额回滚
     *
     * @param mchId
     * @param money
     */
    @Transactional
    public void rollback(Long mchId, int money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.recharge = wallet.recharge - money;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
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
        wallet.recharge = wallet.recharge - money - cashCharge;
        wallet.pending = wallet.pending + money + cashCharge;
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

    @Transactional
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
    }

    /**
     * 充值代付池
     */
    @Transactional
    public Object recharge(Long mchId, Integer money, String password) {
        if (!password.equals(sysPwd)) {
            throw new AwesomeException(Config.ERROR_VERIFY);
        }
        Wallet wallet = getOwnWallet(mchId);
        wallet.reservoir = wallet.reservoir + money;
        walletMapper.updateByPrimaryKeySelective(wallet);
        return wallet;
    }

    /**
     * 适用 cnt 商户余额转换
     * 将子账户subId 余额 转换至 主账户 primaryId
     */
    public void rechargeConvert(Long primaryId, Long subId) {
        Wallet wallet = getOwnWallet(primaryId);

        Wallet decr = getOwnWallet(subId);
        wallet.recharge = wallet.recharge + decr.recharge;
        wallet.total = wallet.recharge + wallet.bonus + wallet.pending;

        decr.recharge = 0;
        decr.total = decr.recharge + decr.pending + decr.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
        walletMapper.updateByPrimaryKeySelective(decr);
    }
}
