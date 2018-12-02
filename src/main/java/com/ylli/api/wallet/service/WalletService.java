package com.ylli.api.wallet.service;

import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.xfpay.mapper.XfBillMapper;
import com.ylli.api.xfpay.model.XfBill;
import java.sql.Timestamp;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    @Autowired
    WalletMapper walletMapper;

    @Autowired
    XfBillMapper xfBillMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Transactional
    public boolean preOrder(Long userId, Integer amount) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet = walletMapper.selectOne(wallet);
        if (wallet == null) {
            return true;
        }
        wallet.avaliableMoney = wallet.avaliableMoney - amount;
        wallet.abnormalMoney = wallet.abnormalMoney + amount;
        wallet.modifyTime = Timestamp.from(Instant.now());
        walletMapper.updateByPrimaryKeySelective(wallet);
        return false;
    }

    @Transactional
    public void finishOrder(Long billId, Integer amount) {
        XfBill bill = xfBillMapper.selectByPrimaryKey(billId);
        if (bill == null || bill.status == XfBill.FINISH) {
            return;
        }
        Wallet wallet = new Wallet();
        wallet.userId = bill.userId;
        wallet = walletMapper.selectOne(wallet);
        if (wallet == null) {
            return;
        }
        wallet.abnormalMoney = wallet.abnormalMoney - amount;
        wallet.totalMoney = wallet.avaliableMoney + wallet.abnormalMoney;
        wallet.modifyTime = Timestamp.from(Instant.now());
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    @Transactional
    public void failedOrder(Long billId, Integer amount) {
        XfBill bill = xfBillMapper.selectByPrimaryKey(billId);
        if (bill == null || bill.status == XfBill.FAIL) {
            return;
        }
        Wallet wallet = new Wallet();
        wallet.userId = bill.userId;
        wallet = walletMapper.selectOne(wallet);
        if (wallet == null) {
            return;
        }
        wallet.abnormalMoney = wallet.abnormalMoney - amount;
        wallet.avaliableMoney = wallet.avaliableMoney + amount;
        wallet.modifyTime = Timestamp.from(Instant.now());
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    @Transactional
    public Wallet incr(Long userId, Integer money) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet = walletMapper.selectOne(wallet);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.userId = userId;
            wallet.totalMoney = money;
            wallet.avaliableMoney = money;
            wallet.abnormalMoney = 0;
            walletMapper.insertSelective(wallet);
        } else {
            wallet.avaliableMoney = wallet.avaliableMoney + money;
            wallet.totalMoney = wallet.totalMoney + money;
            wallet.modifyTime = Timestamp.from(Instant.now());
            walletMapper.updateByPrimaryKeySelective(wallet);
        }
        return walletMapper.selectByUserId(userId);
    }

    @Transactional
    public Object getWallet(Long userId) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet = walletMapper.selectOne(wallet);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.userId = userId;
            wallet.totalMoney = 0;
            wallet.avaliableMoney = 0;
            wallet.abnormalMoney = 0;
            walletMapper.insertSelective(wallet);
            wallet = walletMapper.selectByUserId(userId);
        }
        return wallet;
    }
}
