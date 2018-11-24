package com.ylli.api.wallet.service;

import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    WalletMapper walletMapper;

    public Wallet getOwnWallet(Long userId) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet = walletMapper.selectOne(wallet);
        //默认插入
        if (wallet == null) {
            Wallet w = new Wallet();
            w.userId = userId;
            w.totalMoney = 0;
            w.abnormalMoney = 0;
            w.avaliableMoney = 0;
            walletMapper.insertSelective(w);
            return walletMapper.selectOne(wallet);
        }
        return wallet;
    }
}
