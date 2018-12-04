package com.ylli.api.wallet.service;

import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.user.model.UserChargeInfo;
import com.ylli.api.wallet.mapper.WalletLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.model.WalletLog;
import com.ylli.api.xfpay.mapper.XfBillMapper;
import com.ylli.api.yfbpay.mapper.YfbBillMapper;
import com.ylli.api.yfbpay.model.YfbBill;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
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

    @Autowired
    YfbBillMapper yfbBillMapper;

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
            wallet.bonus = 0d;
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

    @Transactional
    public void automaticBonus(Long userId, Integer type, Integer rate) {
        Wallet wallet = walletMapper.selectByPrimaryKey(userId);
        if (wallet == null) {
            Wallet w = new Wallet();
            w.id = userId;
            walletMapper.insertSelective(w);
            wallet = walletMapper.selectByPrimaryKey(userId);
        }
        /**
         * todo 后面会加上通道管理...(应用，不同的应用设置不同的费率)
         */
        YfbBill bill = new YfbBill();
        bill.userId = userId;
        bill.status = YfbBill.FINISH;
        List<YfbBill> list = yfbBillMapper.select(bill);

        double bonus = 0;
        for (int i = 0; i < list.size(); i++) {
            //
            bill = list.get(i);
            Double item = getBonus(bill.amount, type, rate);
            bonus = bonus + item;
            bill.bonusMoney = item;
            yfbBillMapper.updateByPrimaryKeySelective(bill);
        }
        wallet.bonus = bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    public Double getBonus(Integer money, Integer type, Integer rate) {
        if (type == UserChargeInfo.TYPE_FIX) {
            return Double.valueOf(rate);
        } else {
            return money / 10000.0 * rate;
        }
    }


    @Transactional
    public void addBonus(Long userId, Double bonusMoney, Long billId, Integer rate) {
        Wallet wallet = walletMapper.selectByPrimaryKey(userId);
        if (wallet == null) {
            //出现异常  直接return..  后期分润计算  查找状态已完成的订单，分润金额没有..ok 加上
            return;
        }
        YfbBill bill = yfbBillMapper.selectByPrimaryKey(billId);
        bill.bonusMoney = getBonus(bill.amount, rate);
        yfbBillMapper.updateByPrimaryKeySelective(bill);
        wallet.bonus = wallet.bonus + bonusMoney;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }


    //默认的分润 百分比
    public Double getBonus(Integer money, Integer rate) {
        return money / 10000.0 * rate;
    }
}
