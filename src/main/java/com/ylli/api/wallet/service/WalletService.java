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
}
