package com.ylli.api.cnt;

import com.google.gson.Gson;
import com.ylli.api.pay.enums.Version;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.model.CntCard;
import com.ylli.api.third.pay.model.CntCashReq;
import com.ylli.api.third.pay.model.CntRes;
import com.ylli.api.third.pay.model.OrderConfirm;
import com.ylli.api.third.pay.service.CntClient;
import com.ylli.api.third.pay.service.CntService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class CntPayTest {

    @Autowired
    CntService cntService;

    @Autowired
    CntClient cntClient;

    @Autowired
    PayService payService;

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
        baseOrder.tradeType = "native";
        baseOrder.version = Version.CNT.getVersion();
        Map<String, String> map = SignUtil.objectToMap(baseOrder);
        String secretKey = "97c8890018a34498bc3ab87484d9778e";
        String s1 = SignUtil.generateSignature(map, secretKey);
        System.out.println("key: " + s1);
        System.out.println(new Gson().toJson(baseOrder));
    /*    String order = cntService.createOrder(baseOrder.mchId, 5L, baseOrder.money, baseOrder.mchOrderId, baseOrder.notifyUrl, baseOrder.redirectUrl, baseOrder.reserve, baseOrder.payType, baseOrder.tradeType, baseOrder.extra);
        System.out.println(order);*/
        String s = "";
    }

    @Test
    public void json() throws Exception {
        String s = "{\"data\":{\"date\":1546422897238,\"orderId\":\"O19010217571484\",\"totalPrice\":1.00,\"payPage\":\"https://cntpay.io/\",\"referenceCode\":\"833182\",\"pays\":[{\"payType\":\"0\",\"openBank\":null,\"cardId\":\"154\",\"payUrl\":\"HTTPS://QR.ALIPAY.COM/FKX0009589EIVWZLIUZWE3\",\"subbranch\":null,\"userName\":\"李自由\",\"payName\":\"18957368005\"},{\"payType\":\"1\",\"openBank\":null,\"cardId\":\"155\",\"payUrl\":\"wxp://f2f0Z6zSk0ymkF-W1UB6bLe4A3lrvqcKYK-2\",\"subbranch\":null,\"userName\":\"李自由\",\"payName\":\"ziyou502\"},{\"payType\":\"3\",\"openBank\":\"工商银行\",\"cardId\":\"92\",\"payUrl\":\"\",\"subbranch\":\"嘉兴桐乡支行\",\"userName\":\"自由\",\"payName\":\"6222081204001984016\"}]},\"resultCode\":\"0000\",\"resultMsg\":\"下单成功\"}";
        CntRes cntRes = new Gson().fromJson(s, CntRes.class);
        System.out.println(new Gson().toJson(cntRes));
        CntCard c = new CntCard();
        c.payUrl = "fdasf";
        c.payType = 1;
        List<CntCard> cs = new ArrayList<>();
        cs.add(c);
        cntRes.data.pays = cs;
        System.out.println(new Gson().toJson(cntRes));
    }


    @Test
    public void cash() throws Exception {
        CntCashReq req = new CntCashReq();
//        cashLog.openBank = "pfyh";//"浦发银行";
        req.openBank = "浦发银行";//"浦发银行";
        req.payName = "6217920274920375";
        req.userName = "李玉龙";//"李玉龙";
//        cashLog.name = "chicb";//"李玉龙";
        req.mchId = 1024L;
        req.subbranch = "浦发银行";
//        String s = cntClient.addCard(cashLog.mchId.toString(), cashLog.name, cashLog.bankcardNumber, cashLog.openBank, cashLog.openBank);
        req.money = 1000;
        Object cash = cntService.cash(req);

//        System.out.println(s);
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
        OrderConfirm req = new OrderConfirm();
        req.mchOrderId = "2019010211131000000133";
        req.mchId = 1024L;
        String key = "97c8890018a34498bc3ab87484d9778e";
        Map<String, String> map = SignUtil.objectToMap(req);
        System.out.println(SignUtil.generateSignature(map, key));
        //Response response = (Response) payService.payConfirm(req.mchOrderId, req.mchId);
        //System.out.println(new Gson().toJson(response));
  /*      String confirm = cntClient.confirm(req.mchOrderId,req.mchId.toString());
        System.out.println(confirm);*/
    }

    @Test
    public void cancel() throws Exception {
        String cancel = cntClient.cancel("O19010315521558", "1024");
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
