package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.mch.service.AppService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.model.CTOrderResponse;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CTService {

    private static Logger LOGGER = LoggerFactory.getLogger(CTService.class);

    @Autowired
    BillService billService;

    @Autowired
    BillMapper billMapper;

    @Autowired
    CTClient ctClient;

    @Autowired
    AppService appService;

    @Autowired
    WalletService walletService;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Value("${ct.secret}")
    public String secret;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        String totalFee = String.format("%.2f", (money / 100.0));
        try {
            String str = ctClient.createOrder(totalFee, bill.sysOrderId);
            CTOrderResponse response = new Gson().fromJson(str, CTOrderResponse.class);

            if (!response.result) {
                LOGGER.error("ct order fail: "
                        + "\n mch_order_id : " + mchOrderId + "  sys_order_id : " + bill.sysOrderId
                        + "\n res : " + str);
                //更新订单为失败。
                bill.status = Bill.FAIL;
                billMapper.updateByPrimaryKeySelective(bill);
                return null;
            }
            return response.data;
        } catch (Exception e) {
            //更新订单为失败。
            bill.status = Bill.FAIL;
            billMapper.updateByPrimaryKeySelective(bill);

            return null;
        }
    }

    @Transactional
    public String paynotify(Boolean result, String resultCode, String attach, String totalFee, String sign) throws Exception {

        Boolean flag = signVerify(resultCode, attach, totalFee, sign);

        if (flag) {
            //校验通过

            //resultCode = 1 ;订单状态成功
            Bill bill = billService.selectBySysOrderId(attach);
            if (bill == null) {
                return "order 404 not found.";
            }
            if ("1".equals(resultCode)) {
                if (bill.status != Bill.FINISH) {
                    //不返回上游订单号
                    bill.tradeTime = z8ts();
                    bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;
                    bill.status = Bill.FINISH;
                    //msg暂时先记录实际交易金额/元
                    bill.msg = totalFee;
                    billMapper.updateByPrimaryKeySelective(bill);

                    //钱包金额变动。
                    walletService.incr(bill.mchId, bill.money - bill.payCharge);
                }
            } else {
                bill.tradeTime = z8ts();
                bill.status = Bill.FAIL;
                //msg暂时先记录实际交易金额/元
                bill.msg = totalFee;
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
            return "sign error.";
        }
    }

    public Boolean signVerify(String resultCode, String attach, String totalFee, String sign) throws Exception {
        String str = new StringBuffer().append(resultCode).append(attach).append(totalFee).append(secret).toString();
        return SignUtil.MD5(str).equals(sign.toUpperCase());
    }

    /**
     * return Z8 ts.
     */
    public Timestamp z8ts() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 8);
        Date date = calendar.getTime();
        return new Timestamp(date.getTime());
    }
}
