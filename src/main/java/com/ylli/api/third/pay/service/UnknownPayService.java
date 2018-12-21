package com.ylli.api.third.pay.service;

import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.mch.service.AppService;
import com.ylli.api.wallet.service.WalletService;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableAsync
public class UnknownPayService {

    @Value("${pay.123.token}")
    public String token;

    @Autowired
    BillService billService;

    @Autowired
    UnknownPayClient unknownPayClient;

    @Autowired
    BillMapper billMapper;

    @Autowired
    AppService appService;

    @Autowired
    WalletService walletService;

    @Autowired
    PayService payService;

    @Autowired
    PayClient payClient;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        String price = String.format("%.2f", (money / 100.0));
        //支付渠道 1：支付宝；2：微信支付
        Integer istype = payType.equals(PayService.ALI) ? 1 : 2;
        //1：获取json支付码URL信息，在自己的网站内展示二维码；2：跳转到我们的标准支付页
        String str = unknownPayClient.createOrder(price, istype, redirectUrl, bill.sysOrderId, get20UUID(), 2);

        return str;
    }

    public String get20UUID() {
        UUID id = UUID.randomUUID();
        String[] idd = id.toString().split("-");
        return idd[0] + idd[1] + idd[2] + idd[3];
    }

    @Transactional
    public String payNotify(String orderid, String price, String codeid, String key) throws Exception {
        if (signCheck(codeid, orderid, price, key)) {
            //todo  上游尝试回调3次，每次间隔1分钟，加入回调 sendNotify 。see xfpay
            Bill bill = new Bill();
            bill.sysOrderId = orderid;
            bill = billMapper.selectOne(bill);
            if (bill == null) {
                return "order not found";
            }
            if (bill.status != Bill.FINISH) {
                bill.status = Bill.FINISH;
                bill.tradeTime = Timestamp.from(Instant.now());
                bill.payCharge = (bill.money * appService.getRate(bill.mchId, bill.appId)) / 10000;

                //不返回上游订单号. 暂时使用生成二维码是随机字符串做上游订单号
                bill.superOrderId = codeid;
                bill.msg = price;
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

            /*if (bill.isSuccess != null && bill.isSuccess) {
                return "success";
            } else {
                //隐藏真实返回.待商户系统返回success再做正确处理
                return "";
            }*/
            return "success";
        } else {
            return "sign error";
        }
    }

    public Boolean signCheck(String codeid, String orderid, String price, String key) throws Exception {
        StringBuffer sb = new StringBuffer()
                .append(codeid).append(orderid).append(price).append(token);
        return SignUtil.MD5(sb.toString()).equals(key.toUpperCase());
    }
}
