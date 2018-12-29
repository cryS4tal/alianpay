package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.ylli.api.mch.service.AppService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;

@Service
@EnableAsync
public class CntService {

    @Value("${pay.cnt.secret}")
    public String secret;

    @Autowired
    BillService billService;
    @Autowired
    CntClient cntClient;
    @Autowired
    PayClient payClient;
    @Autowired
    PayService payService;
    @Autowired
    BillMapper billMapper;
    @Autowired
    WalletService walletService;
    @Autowired
    AppService appService;
    @Value("pay.cont.success_code")
    public String successCode;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);
        String mz = String.format("%.2f", (money / 100.0));
        String istype = payType.equals(PayService.ALI) ? "0" : "1";
        return cntClient.createCntOrder(bill.sysOrderId, mchId.toString(), mz, istype);
    }

    public String payConfirm(String orderId) {
        Bill bill = new Bill();
        bill.sysOrderId = orderId;
        bill = billMapper.selectOne(bill);
        if (bill == null || Strings.isNullOrEmpty(bill.superOrderId)) {
            return "order not found";
        }
        if("ok".equals(cntClient.confirm(bill.superOrderId))){
            if (bill.status != Bill.FINISH) {
                bill.status = Bill.FINISH;
                bill.tradeTime = Timestamp.from(Instant.now());
                bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                billMapper.updateByPrimaryKeySelective(bill);
                //钱包金额变动。
                walletService.incr(bill.mchId, bill.money - bill.payCharge);
            }
            return "success";
        }
        return "fail";
    }

    /**
     * @param userId     商户号
     * @param orderId    订单号
     * @param userOrder  用户系统订单号
     * @param number     数量
     * @param date       时间
     * @param resultCode 状态码 0000 成功 其他为失败
     * @param resultMsg  状态描述
     * @param appID      APPID
     * @return
     */
    @Transactional
    public String payNotify(Long userId, String orderId, String userOrder, String number, String sn, String date, String resultCode, String resultMsg, String appID, String chkValue) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(userId).append("|").append(orderId).append("|").append(userOrder).append("|").append(number).append("|").append(date).append("|")
                .append(resultCode).append("|").append(resultCode).append("|").append(resultMsg).append("|").append(resultMsg).append("|").append("|").append(secret);
        String sign = SignUtil.MD5(sb.toString());
        System.out.println(sign);
        if (successCode.equals(resultCode) && chkValue.equals(sign)) {
            Bill bill = new Bill();
            bill.sysOrderId = orderId;
            bill = billMapper.selectOne(bill);
            if (bill == null) {
                return "order not found";
            }
            if (bill.status != Bill.FINISH) {
                bill.status = Bill.FINISH;
                bill.tradeTime = Timestamp.from(Instant.now());
                bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                bill.superOrderId = userOrder;
                bill.msg = resultMsg;
                billMapper.updateByPrimaryKeySelective(bill);

                //钱包金额变动。
                walletService.incr(bill.mchId, bill.money - bill.payCharge);
            }

            //加入异步通知下游商户系统
            //params jsonStr.
            String params = payService.generateRes(
                    bill.money.toString(),
                    bill.mchOrderId,
                    bill.sysOrderId,
                    bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                    bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                    bill.reserve);

            payClient.sendNotify(bill.id, bill.notifyUrl, params, true);

            return "ok";
        } else {
            return "fail";
        }

    }
}
