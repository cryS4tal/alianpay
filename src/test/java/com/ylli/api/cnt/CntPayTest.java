package com.ylli.api.cnt;

import com.google.gson.Gson;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.model.ConfirmReq;
import com.ylli.api.third.pay.service.CntClient;
import com.ylli.api.third.pay.service.CntService;
import com.ylli.api.wallet.model.CashLog;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Ignore
public class CntPayTest {
    @Autowired
    CntService cntService;
    @Autowired
    CntClient cntClient;

    @Autowired
    SerializeUtil serializeUtil;

    @Test
    public void createCntOrder() throws Exception {
        BaseOrder baseOrder = new BaseOrder();
        baseOrder.mchId = 1024L;
        baseOrder.money = 100;
        baseOrder.mchOrderId = serializeUtil.generateSysOrderId();
        baseOrder.notifyUrl = "";
        baseOrder.redirectUrl = "";
        baseOrder.reserve = "";
        baseOrder.payType = "alipay";
        baseOrder.tradeType = "";
        String order = cntService.createOrder(baseOrder.mchId, 5L, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl, baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);
        System.out.println(order);
        String s = "";
    }

    @Test
    public void cash() throws Exception {
        String cntOrder = cntClient.createCntOrder(serializeUtil.generateSysOrderId(), "1024", "1.00", "0", "0");
        System.out.println(cntOrder);
    }

    @Test
    public void addCard() throws Exception {
        CashLog cashLog = new CashLog();
//        cashLog.openBank = "pfyh";//"浦发银行";
        cashLog.openBank = "浦发银行";//"浦发银行";
        cashLog.bankcardNumber = "6217920274920375";
        cashLog.name = "李玉龙";//"李玉龙";
//        cashLog.name = "chicb";//"李玉龙";
        cashLog.mchId = 1024L;
        String s = cntClient.addCard(cashLog.mchId.toString(), cashLog.name, cashLog.bankcardNumber, cashLog.openBank, cashLog.openBank);
        System.out.println(s);
    }
    @Test
    public void findCards() throws Exception {
        String cards = cntClient.findCards("1024");
        System.out.println(cards);
    }

    @Test
    public void delCard() throws Exception {
        String cards = cntClient.delCard("376");
        System.out.println(cards);
    }

    @Test
    public void confirm() throws Exception {
        ConfirmReq req = new ConfirmReq();
        req.orderId = "O18122919380920";
        req.cardId = "334";
        String confirm = cntClient.confirm(req.orderId, req.cardId);
        System.out.println(confirm);
    }

    @Test
    public void cancel() throws Exception {
        String cancel = cntClient.cancel("O18122919380920", "1024");
        System.out.println(cancel);
    }

    @Test
    public void create() throws Exception {
        BaseOrder base = new BaseOrder();
        base.mchId = 1024L;
        base.payType = "alipay";
        base.mchOrderId = serializeUtil.generateSysOrderId();
        base.money = 100;
        base.tradeType = "like";
        Map<String, String> map = SignUtil.objectToMap(base);
        String s = SignUtil.generateSignature(map, "97c8890018a34498bc3ab87484d9778e");
        System.out.println(s);
        System.out.println(new Gson().toJson(base));
    }

}
