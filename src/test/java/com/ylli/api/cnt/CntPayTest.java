package com.ylli.api.cnt;

import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.third.pay.model.ConfirmReq;
import com.ylli.api.third.pay.service.CntClient;
import com.ylli.api.third.pay.service.CntService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
        BaseOrder baseOrder=new BaseOrder();
        baseOrder.mchId=1024L;
        baseOrder.money=100;
        baseOrder.mchOrderId=serializeUtil.generateSysOrderId();
        baseOrder.notifyUrl="";
        baseOrder.redirectUrl="";
        baseOrder.reserve="";
        baseOrder.payType="alipay";
        baseOrder.tradeType="";
        String order = cntService.createOrder(baseOrder.mchId, 5L, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl, baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);
        System.out.println(order);
        String s="";
    }
    @Test
    public void confirm() throws Exception {
        ConfirmReq req=new ConfirmReq();
        req.orderId="O18122919380920";
        req.cardId="334";
        String confirm = cntClient.confirm(req.orderId, req.cardId);
        System.out.println(confirm);
    }

    @Test
    public void cancel() throws Exception{
        String cancel = cntClient.cancel("O18122919380920", "1024");
        System.out.println(cancel);
    }

}
