package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.service.AppService;
import com.ylli.api.mch.service.MchKeyService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SerializeUtil;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.model.CntRes;
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
    @Autowired
    MchKeyService mchKeyService;
    @Value("${pay.cont.success_code}")
    public String successCode;
    @Autowired
    SerializeUtil serializeUtil;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);
        String sysOrderId = serializeUtil.generateSysOrderId();
        String mz = String.format("%.2f", (money / 100.0));
        String istype = payType.equals(PayService.ALI) ? "0" : "1";
        String body = cntClient.createCntOrder(sysOrderId, mchId.toString() + "_" + mchOrderId, mz, istype, "1");
        CntRes cntRes = new Gson().fromJson(body, CntRes.class);
        if (successCode.equals(cntRes.resultCode)) {
            billService.createCntBill(cntRes.orderId, sysOrderId, mchId, mchOrderId, channelId, payType, tradeType, money, reserve);
        }
        return body;
    }

    public String payConfirm(String cardId, String orderId, String sign, Long mchId) throws Exception {
        String key = mchKeyService.getKeyById(mchId);
        if (!sign.equals(generateCkValue(mchId.toString(), orderId, key))) {
            return "fail";
        }
        Bill bill = new Bill();
        bill.sysOrderId = orderId;
        bill = billMapper.selectOne(bill);
        if (bill == null || Strings.isNullOrEmpty(bill.superOrderId)) {
            return "order not found";
        }
        String confirm = cntClient.confirm(bill.superOrderId, cardId);
        CntRes cntRes = new Gson().fromJson(confirm, CntRes.class);
        if (successCode.equals(cntRes.resultCode)) {
           /* if (bill.status != Bill.FINISH) {
                bill.status = Bill.FINISH;
                bill.tradeTime = Timestamp.from(Instant.now());
                bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                billMapper.updateByPrimaryKeySelective(bill);
                //钱包金额变动。
                walletService.incr(bill.mchId, bill.money - bill.payCharge);
            }*/
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
    public String payNotify(Long userId, String orderId, String userOrder, String number, String remark, String merPriv, String date, String resultCode, String resultMsg, String appID, String chkValue) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(userId).append("|").append(orderId).append("|").append(userOrder).append("|").append(number).append("|").append(remark).append("|").append(merPriv).append("|").append(date).append("|")
                .append(resultCode).append("|").append(resultCode).append("|").append(resultMsg).append("|").append(resultMsg).append("|").append("|").append(secret);
        String sign = SignUtil.MD5(sb.toString()).toLowerCase();
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

            return "success";
        } else {
            return "fail";
        }

    }

    @Transactional
    public Object payCancel(String orderId, Long mchId, String sgin) throws Exception {
        String key = mchKeyService.getKeyById(mchId);
        if (!sgin.equals(generateCkValue(mchId.toString(), orderId, key))) {
            return "fail";
        }
        Bill bill = new Bill();
        bill.sysOrderId = orderId;
        bill = billMapper.selectOne(bill);
        if (bill == null || Strings.isNullOrEmpty(bill.superOrderId)) {
            return "order not found";
        }
        String confirm = cntClient.cancel(bill.superOrderId, mchId.toString());
        CntRes cntRes = new Gson().fromJson(confirm, CntRes.class);
        if (successCode.equals(cntRes.resultCode)) {
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);
            return "success";
        }
        return "fail";
    }

    public String generateCkValue(String... arr) throws Exception {
        StringBuffer sb = new StringBuffer();
        for (String s : arr) {
            sb.append(s).append("|");
        }
        String substring = sb.substring(0, sb.length() - 1);
        System.out.println(substring);
        return SignUtil.MD5(substring).toLowerCase();
    }

    public Object addCard(Long mchId, String userName, String payName, String openBank, String subbranch) throws Exception {
        cntClient.addCard(mchId.toString(), userName, payName, openBank, subbranch);
        return "fail";
    }
}
