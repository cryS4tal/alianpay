package com.ylli.api.wallet.service;

import com.ylli.api.user.model.UserChargeInfo;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.third.pay.mapper.YfbBillMapper;
import com.ylli.api.third.pay.model.YfbBill;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

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
    public void automaticBonus(Long userId, Integer type, Integer rate) {
        Wallet wallet = walletMapper.selectByPrimaryKey(userId);

        // todo  默认插入代码 后续记得删除.
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

        /**
         * 之前设定是分润，现在是充值金额。
         */
        //double bonus = 0;
        Double recharge = 0D;
        for (int i = 0; i < list.size(); i++) {
            //
            bill = list.get(i);
            //现在是手续费
            Double item = getBonus(bill.amount, type, rate);
            bill.bonusMoney = item;
            yfbBillMapper.updateByPrimaryKeySelective(bill);
            //bonus = bonus + item;
            recharge = recharge + (bill.amount - item);
        }
        //wallet.bonus = bonus;
        wallet.recharge = recharge.intValue();
        wallet.total = wallet.bonus + wallet.recharge;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }

    /**
     * todo 原始设定是分润金额，  现在实际是手续费。相反的逻辑。
     * getBonus  后续变更成  get手续费
     * 记得移除 type..目前全部是百分比
     */
    public Double getBonus(Integer money, Integer type, Integer rate) {
        if (type == UserChargeInfo.TYPE_FIX) {
            return Double.valueOf(rate);
        } else {
            return money / 10000.0 * rate;
        }
    }


    /**
     *
     */
    @Transactional
    public void addBonus(Long userId, Long billId, Integer rate) {
        Wallet wallet = walletMapper.selectByPrimaryKey(userId);
        if (wallet == null) {
            //出现异常  直接return..  后期分润计算  查找状态已完成的订单，分润金额没有..ok 加上
            return;
        }
        YfbBill bill = yfbBillMapper.selectByPrimaryKey(billId);
        //更新分润变成更新手续费。
        bill.bonusMoney = getBonus(bill.amount, rate);
        yfbBillMapper.updateByPrimaryKeySelective(bill);

        //wallet.bonus = wallet.bonus + bonusMoney;
        wallet.recharge = wallet.recharge + (bill.amount - getBonus(bill.amount, rate).intValue());

        wallet.total = wallet.bonus + wallet.recharge;
        walletMapper.updateByPrimaryKeySelective(wallet);
    }


    //默认的分润 百分比  todo  现在时手续费
    public Double getBonus(Integer money, Integer rate) {
        return money / 10000.0 * rate;
    }

    @Transactional
    public void incr(Long mchId, int money) {
        Wallet wallet = walletMapper.selectByPrimaryKey(mchId);
        wallet.recharge = wallet.recharge + money;
        wallet.total = wallet.recharge + wallet.pending + wallet.bonus;
        walletMapper.updateByPrimaryKeySelective(wallet);
        // todo 加入钱包金额变动 关联 账单日志表。  wallet log
    }
}
