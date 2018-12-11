package com.ylli.api.release;

import com.ylli.api.auth.mapper.AccountMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.user.mapper.UserSettlementMapper;
import com.ylli.api.user.model.UserSettlement;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.WalletMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.Wallet;
import com.ylli.api.wallet.service.WalletService;
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

    @PostConstruct
    void init() {
        fixWallet();
        fullCashWithWallet();
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
}
