package com.ylli.api.third.pay.service.dh;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.modelVo.dh.DHNotify;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DHService {

    private static Logger LOGGER = LoggerFactory.getLogger(DHService.class);

    @Autowired
    BillService billService;

    @Value("${dh.sercret}")
    public String secret;

    @Autowired
    DHClient dhClient;

    @Autowired
    RateService rateService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        //分转换元
        String price = String.format("%.2f", (money / 100.0));

        String response = dhClient.createOrder(bill.sysOrderId, price);

        return response;

    }

    @Transactional
    public String dhnotify(DHNotify notify) throws Exception {

        LOGGER.info(new Gson().toJson(notify));

        Boolean flag = signCheck(notify);

        if (true) {
            Bill bill = billService.selectBySysOrderId(notify.orderid);
            if (bill == null) {
                return "order 404 not found";
            }
            if (bill.status == Bill.FINISH || bill.status == Bill.FAIL) {
                return "order finish";
            }

            if ("00".equals(notify.returncode)) {
                if (bill.status != Bill.FINISH) {
                    bill.tradeTime = new Timestamp(System.currentTimeMillis());
                    bill.payCharge = (bill.money * rateService.getRate(bill.mchId, bill.appId)) / 10000;
                    bill.status = Bill.FINISH;
                    bill.msg = notify.amount;
                    bill.superOrderId = notify.transactionid;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);
                }
            } else {
                bill.tradeTime = new Timestamp(System.currentTimeMillis());
                bill.status = Bill.FAIL;
                //bill.msg = msg;
                billMapper.updateByPrimaryKeySelective(bill);
            }

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
            return "ok";
        } else {
            return "sign error";
        }
    }

    public Boolean signCheck(DHNotify notify) throws Exception {
        Map<String, String> map = SignUtil.objectToMap(notify);

        Set<String> keySet = map.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign") || k.equals("attach")) {
                continue;
            }
            sb.append(k).append("=").append(map.get(k)).append("&");
        }
        sb.append("key=").append(secret);

        String sign = SignUtil.MD5(sb.toString());
        return sign.equals(notify.sign);
    }
}
