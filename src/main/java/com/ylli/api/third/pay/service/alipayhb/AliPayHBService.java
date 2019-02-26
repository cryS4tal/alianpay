package com.ylli.api.third.pay.service.alipayhb;

import com.google.common.base.Strings;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AliPayHBService {

    @Autowired
    BillService billService;

    @Autowired
    AliPayHBClient aliPayHBClient;

    @Autowired
    RateService appService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    WalletService walletService;

    @Autowired
    PayClient payClient;

    @Autowired
    PayService payService;

    @Value("${zfbhb.secret}")
    public String secret;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        //分转换元
        String price = String.format("%.2f", (money / 100.0));

        String response = aliPayHBClient.createOrder(bill.sysOrderId, price);

        return response;

    }

    @Transactional
    public String zfbhbnotify(String merchantId, String outTradeNo, String tradeNo, String payTime, String tranAmt,
                              String settleStatus, String payStatus, String msg, String timeStamp, String sign) throws Exception {
        //sign check
        Map<String, String> map = new HashMap<>();
        map.put("merchantId", merchantId);
        map.put("outTradeNo", outTradeNo);
        map.put("tradeNo", tradeNo);
        map.put("payTime", payTime);
        map.put("tranAmt", tranAmt);
        map.put("settleStatus", settleStatus);
        map.put("payStatus", payStatus);
        map.put("msg", msg);
        map.put("timeStamp", timeStamp);
        Boolean flag = generateSignature(map, secret).equals(sign);

        if (flag) {
            Bill bill = billService.selectBySysOrderId(outTradeNo);
            if (bill == null) {
                return "order 404 not found";
            }
            if (bill.status == Bill.FINISH || bill.status == Bill.FAIL) {
                return "order finish";
            }

            if ("0000".equals(payStatus)) {
                if (bill.status != Bill.FINISH) {
                    bill.tradeTime = new Timestamp(System.currentTimeMillis());
                    bill.superOrderId = tradeNo;
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    bill.status = Bill.FINISH;
                    bill.msg = tranAmt;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);
                }
            } else {
                bill.tradeTime = new Timestamp(System.currentTimeMillis());
                bill.status = Bill.FAIL;
                bill.superOrderId = tradeNo;
                bill.msg = msg;
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
            return "success";
        } else {
            return "sign error";
        }
    }

    public String generateSignature(final Map<String, String> data, String key) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) {
                continue;
            }
            sb.append(k).append("=").append(data.get(k)).append("&");
        }
        sb.append("key=").append(key);

        String sign = SignUtil.MD5(sb.toString()).toLowerCase();
        return sign;
    }
}
