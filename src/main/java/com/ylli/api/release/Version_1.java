package com.ylli.api.release;

import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.user.mapper.UserSettlementMapper;
import com.ylli.api.user.model.UserSettlement;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.service.WalletService;
import com.ylli.api.third.pay.mapper.YfbBillMapper;
import com.ylli.api.third.pay.model.YfbBill;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于v1.0 版本更新纠正数据.
 */
@Component
public class Version_1 {

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    WalletMapper walletMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    UserSettlementMapper userSettlementMapper;

    @Autowired
    YfbBillMapper yfbBillMapper;

    @Autowired
    BillMapper billMapper;

    @PostConstruct
    void init() {
        //fixWallet();
        //fullCashWithWallet();
        //fixBill();
    }

    /**
     * 1.0 版本数据纠正..
     * 修正初版用户注册时未初始化钱包数据.
     */
    void fixWallet() {
        List<Account> list = accountMapper.selectAll();
        for (int i = 0; i < list.size(); i++) {
            Wallet wallet = walletMapper.selectByPrimaryKey(list.get(i).id);
            if (wallet == null) {
                walletService.init(list.get(i).id);
            }
        }
    }

    /**
     * 1.0 版本修改提现请求，关联到钱包
     * 提现请求新增 四要素信息，每次记录；对待处理的请求，扣除钱包中交易金额（recharge），加入到待处理金额
     * 对历史提现信息，记录原 settlement 记录的结算信息。
     * todo settlement 保留？记录用户每次提现的账户信息
     */
    void fullCashWithWallet() {
        List<CashLog> logs = cashLogMapper.selectAll();
        logs.stream().forEach(item -> {
            UserSettlement settlement = userSettlementMapper.selectByUserId(item.mchId);
            item.bankcardNumber = settlement.bankcardNumber;
            item.identityCard = settlement.identityCard;
            item.name = settlement.name;
            item.reservedPhone = settlement.reservedPhone;
            item.openBank = settlement.openBank;
            item.subBank = settlement.subBank;
            cashLogMapper.updateByPrimaryKeySelective(item);
        });
    }

    /**
     * 将原先账单 t_yfb_bill 数据迁移至 t_bill
     */
    void fixBill() {
        YfbBill yfbBill = new YfbBill();
        yfbBill.status = YfbBill.FINISH;
        List<YfbBill> list = yfbBillMapper.select(yfbBill);
        list.stream().forEach(item -> {
            Bill exist = new Bill();
            exist.sysOrderId = item.orderNo;
            exist = billMapper.selectOne(exist);
            if (exist != null) {
                return;
            }
            Bill bill = new Bill();
            bill.mchId = item.userId;
            bill.sysOrderId = item.orderNo;
            bill.mchOrderId = item.subNo;
            bill.superOrderId = item.superNo;
            bill.appId = 0l;
            bill.channelId = 1l;
            bill.money = item.amount;
            bill.status = item.status;
            bill.reserve = item.memo;
            bill.notifyUrl = item.notifyUrl;
            bill.redirectUrl = item.redirectUrl;
            bill.msg = item.msg;
            bill.isSuccess = item.isSuccess;
            bill.payType = item.payType;
            bill.tradeType = item.tradeType;
            bill.tradeTime = item.tradeTime;
            bill.payCharge = item.bonusMoney.intValue();
            bill.createTime = item.createTime;
            bill.modifyTime = item.modifyTime;
            billMapper.insertSelective(bill);
        });
    }
}
