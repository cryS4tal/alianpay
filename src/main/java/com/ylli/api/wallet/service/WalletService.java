package com.ylli.api.wallet.service;

import com.ylli.api.third.pay.mapper.YfbBillMapper;
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
}
