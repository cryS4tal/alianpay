package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.third.pay.model.EazyNotify;
import com.ylli.api.third.pay.model.EazyResponse;
import com.ylli.api.third.pay.util.EazySignUtil;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EazyPayService {

    @Value("${pay.eazy.key}")
    public String KEY;

    @Autowired
    EazyClient eazyClient;

    @Autowired
    BillService billService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    PayClient payClient;

    @Autowired
    PayService payService;

    @Autowired
    WalletService walletService;

    @Autowired
    RateService appService;

    @Transactional
    public EazyResponse createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl,
                                    String reserve, String payType, String tradeType, Object extra) {

        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        //分转换元
        String amount = String.format("%.2f", (money / 100.0));

        String result = eazyClient.createOrder(bill.sysOrderId, amount);
        EazyResponse response = new Gson().fromJson(result, EazyResponse.class);
        if (200 == response.code) {
            bill.superOrderId = response.data.orderId;
            billMapper.updateByPrimaryKeySelective(bill);
        } else {
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
        }
        return response;
    }

    @Transactional
    public String paynotify(EazyNotify notify) throws Exception {

        Boolean flag = EazySignUtil.encrypt(Double.valueOf(notify.amount), notify.outTradeNo, KEY).equals(notify.sign);

        if (flag) {
            Bill bill = billService.selectBySysOrderId(notify.outTradeNo);
            if (bill == null) {
                return "order not found";
            }
            if (bill.status == Bill.FINISH || bill.status == Bill.FAIL) {
                return "order finish";
            }

            if ("success".equals(notify.status)) {
                if (bill.status != Bill.FINISH) {
                    bill.tradeTime = new Timestamp(System.currentTimeMillis());
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    bill.status = Bill.FINISH;
                    bill.msg = notify.amount;
                    bill.superOrderId = notify.tradeNo;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money - bill.payCharge);
                }

            } else {
                bill.superOrderId = notify.tradeNo;
                bill.tradeTime = new Timestamp(System.currentTimeMillis());
                bill.status = Bill.FAIL;
                bill.msg = notify.amount;
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
            return "success";
        } else {
            return "sign error";
        }
    }
}
