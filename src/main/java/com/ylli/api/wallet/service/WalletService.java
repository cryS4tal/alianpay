package com.ylli.api.wallet.service;

import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.wallet.mapper.WalletLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.model.WalletLog;
import com.ylli.api.xfpay.mapper.XfBillMapper;
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

    @Autowired
    WalletLogMapper walletLogMapper;

    @Autowired
    AuthSession authSession;

    /**
     * 获取用户钱包数据，默认初始化
     *
     * @param userId
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Wallet getOwnWallet(Long userId) {
        Wallet wallet = walletMapper.selectByPrimaryKey(userId);
        //默认插入
        if (wallet == null) {
            Wallet w = new Wallet();
            w.id = userId;
            walletMapper.insertSelective(w);

            return walletMapper.selectByPrimaryKey(userId);
        }
        return wallet;
    }

    /*@Transactional
    public boolean preOrder(Long userId, Integer amount) {
        Wallet wallet = new Wallet();
        //wallet.userId = userId;
        wallet = walletMapper.selectOne(wallet);
        if (wallet == null) {
            return true;
        }
        wallet.avaliableMoney = wallet.avaliableMoney - amount;
        wallet.abnormalMoney = wallet.abnormalMoney + amount;
        wallet.modifyTime = Timestamp.from(Instant.now());
        walletMapper.updateByPrimaryKeySelective(wallet);
        return false;
    }*/

    /*@Transactional
    public void finishOrder(Long billId, Integer amount) {
        XfBill bill = xfBillMapper.selectByPrimaryKey(billId);
        if (bill == null || bill.status == XfBill.FINISH) {
            return;
        }
        Wallet wallet = new Wallet();
        //wallet.userId = bill.userId;
        wallet = walletMapper.selectOne(wallet);
        if (wallet == null) {
            return;
        }
        wallet.abnormalMoney = wallet.abnormalMoney - amount;
        wallet.totalMoney = wallet.avaliableMoney + wallet.abnormalMoney;
        wallet.modifyTime = Timestamp.from(Instant.now());
        walletMapper.updateByPrimaryKeySelective(wallet);
    }*/

    /*@Transactional
    public void failedOrder(Long billId, Integer amount) {
        XfBill bill = xfBillMapper.selectByPrimaryKey(billId);
        if (bill == null || bill.status == XfBill.FAIL) {
            return;
        }
        Wallet wallet = new Wallet();
        //wallet.userId = bill.userId;
        wallet = walletMapper.selectOne(wallet);
        if (wallet == null) {
            return;
        }
        wallet.abnormalMoney = wallet.abnormalMoney - amount;
        wallet.avaliableMoney = wallet.avaliableMoney + amount;
        wallet.modifyTime = Timestamp.from(Instant.now());
        walletMapper.updateByPrimaryKeySelective(wallet);
    }*/

    @Transactional
    public Wallet incr(Long userId, Integer money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(userId);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.id = userId;
            walletMapper.insertSelective(wallet);
        } else {
            wallet.bonus = wallet.bonus + money;
            wallet.total = wallet.total + money;
            wallet.modifyTime = Timestamp.from(Instant.now());
            walletMapper.updateByPrimaryKeySelective(wallet);
        }
        WalletLog log = new WalletLog();
        log.adminId = authSession.getAuthId();
        log.userId = userId;
        log.type = WalletLog.CZ;
        log.money = money;
        log.currentMoney = wallet.total;
        walletLogMapper.insertSelective(log);

        return walletMapper.selectByPrimaryKey(userId);
    }

    @Transactional
    public Wallet getWallet(Long userId) {
        Wallet wallet = walletMapper.selectByPrimaryKey(userId);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.id = userId;
            wallet.total = 0;
            wallet.bonus = 0;
            wallet.recharge = 0;
            walletMapper.insertSelective(wallet);
            wallet = walletMapper.selectByPrimaryKey(userId);
        }
        return wallet;
    }

    @Transactional
    public void create(Long id) {
        Wallet wallet = new Wallet();
        wallet.id = id;
        walletMapper.insertSelective(wallet);
    }
}
