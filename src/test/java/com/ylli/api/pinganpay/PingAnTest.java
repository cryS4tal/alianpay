package com.ylli.api.pinganpay;

import com.ylli.api.third.pay.service.PingAnService;
import com.ylli.api.wallet.mapper.CashLogMapper;
import com.ylli.api.wallet.mapper.SysPaymentLogMapper;
import com.ylli.api.wallet.model.CashLog;
import com.ylli.api.wallet.model.SysPaymentLog;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class PingAnTest {

    @Autowired
    PingAnService service;

    @Autowired
    CashLogMapper cashLogMapper;

    @Autowired
    SysPaymentLogMapper sysPaymentLogMapper;

    @Test
    public void createOrder() {

        //插入测试数据.
        CashLog cashLog = new CashLog();
        cashLog.mchId = 1002L;
        cashLog.money = 100;
        cashLog.openBank = "浦发银行";
        cashLog.bankcardNumber = "6217920274920375";
        cashLog.name = "李玉龙";
        cashLog.state = 0;
        cashLogMapper.insertSelective(cashLog);
        service.createPingAnOrder(cashLog.id, cashLog.bankcardNumber, cashLog.name + "1", cashLog.openBank, "", cashLog.money);
    }


    @Test
    public void orderQuery() {

        SysPaymentLog sysPaymentLog = new SysPaymentLog();
        sysPaymentLog.type = SysPaymentLog.TYPE_MCH;
        sysPaymentLog.orderId = "PingAn" + "96";
        sysPaymentLog = sysPaymentLogMapper.selectOne(sysPaymentLog);

        service.payQuery(sysPaymentLog);
    }
}
