package com.ylli.api.third.pay.service.cxt;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.modelVo.cxt.CXTResponse;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CXTService {

    private static Logger LOGGER = LoggerFactory.getLogger(CXTService.class);

    @Autowired
    CXTClient cxtClient;

    @Autowired
    BillService billService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Autowired
    RateService appService;

    @Transactional
    public CXTResponse createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        //分转换元
        String price = String.format("%.2f", (money / 100.0));

        CXTResponse response = cxtClient.createOrder(bill.sysOrderId, price, redirectUrl);

        return response;
    }

    @Transactional
    public String payNotify(CXTNotify notify) throws Exception {
        LOGGER.info(new Gson().toJson(notify));

        Boolean flag = verify(notify);
        if (flag) {
            Bill bill = billService.selectBySysOrderId(notify.orderid);
            if (bill == null) {
                throw new RuntimeException("order 404 not found");
            }
            if (bill.status == Bill.FINISH || bill.status == Bill.FAIL) {
                throw new RuntimeException("order finish");
            }

            bill.tradeTime = new Timestamp(System.currentTimeMillis());
            bill.superOrderId = notify.ordno;
            bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
            bill.status = Bill.FINISH;
            bill.msg = notify.realprice;
            billMapper.updateByPrimaryKeySelective(bill);

            //钱包金额变动。
            walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);

            //加入异步通知下游商户系统
            //params jsonStr.
            if (!Strings.isNullOrEmpty(bill.notifyUrl)) {
                String params = payService.generateRes(
                        bill.money.toString(),
                        bill.mchOrderId,
                        bill.sysOrderId,
                        bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                        bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                        bill.reserve);

                payClient.sendNotify(bill.id, bill.notifyUrl, params, true);
            }

            return "success";
        } else {
            throw new RuntimeException("sign error");
        }
    }

    /*public static void main(String[] args) throws Exception {
        String str = "{\"uid\":\"bb74925f-26ad-4626-9982-b21dce642254\",\"key\":\"233220dfb77036e749b522ea8f721538\",\"orderuid\":\"goodsname descript\",\"ordno\":\"1f0c2998-9884-4bce-bf58-a94d1d4b0919\",\"realprice\":\"1.04\",\"price\":\"1.0\",\"orderid\":\"2019022102300500000003\"}";
        CXTNotify notify = new Gson().fromJson(str, CXTNotify.class);

        String sign = generateKey(notify);
        System.out.println(sign.equals(notify.key));
    }*/

    public Boolean verify(CXTNotify notify) throws Exception {

        Map<String, String> data = SignUtil.objectToMap(notify);
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("format") || k.equals("key")) {
                continue;
            }
            if (data.get(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(data.get(k));
        }
        return SignUtil.MD5(sb.toString()).toLowerCase().equals(notify.key);
    }

}
