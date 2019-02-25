package com.ylli.api.third.pay.service.alipayh5;

import com.google.common.base.Strings;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import com.ylli.api.pay.service.PayClient;
import com.ylli.api.pay.service.PayService;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.wallet.service.WalletService;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlipayH5Service {

    private static Logger LOGGER = LoggerFactory.getLogger(AlipayH5Service.class);

    @Value("${alipayh5.appid}")
    private String appid;

    @Value("${alipayh5.token}")
    private String token;

    @Autowired
    BillService billService;

    @Autowired
    AlipayH5Client alipayH5Client;

    @Autowired
    RateService rateService;

    @Autowired
    WalletService walletService;

    @Autowired
    PayClient payClient;

    @Autowired
    PayService payService;

    @Autowired
    BillMapper billMapper;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        //分转换元
        String price = String.format("%.2f", (money / 100.0));

        String response = alipayH5Client.createOrder(bill.sysOrderId, price);

        return response;
    }

    @Transactional
    public void zfbh5notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //接收参数
        Map parameters = request.getParameterMap();// 保存request中参数的临时变量
        Map<String, String> params = new HashMap<String, String>();// 参与签名业务字段集合
        Iterator paiter = parameters.keySet().iterator();
        while (paiter.hasNext()) {
            String key = paiter.next().toString();
            String[] values = (String[]) parameters.get(key);
            params.put(key, values[0]);
        }


        PrintWriter writer = response.getWriter();

        Boolean flag = signCheck(params.get("order_no"), params.get("amount"), params.get("receipt_amount"), params.get("pay_time"), params.get("trade_no")).equals(params.get("sign"));

        if (flag) {
            Bill bill = billService.selectBySysOrderId(params.get("order_no").toString());
            if (bill == null) {
                writer.write("order 404 not found");
                return;
            }
            if (bill.status == Bill.FINISH || bill.status == Bill.FAIL) {
                writer.write("order finish");
                return;
            }

            if (bill.status != Bill.FINISH) {
                bill.tradeTime = new Timestamp(System.currentTimeMillis());
                bill.payCharge = (bill.money * rateService.getRate(bill.mchId, bill.appId)) / 10000;
                bill.status = Bill.FINISH;
                bill.msg = params.get("receipt_amount");
                bill.superOrderId = params.get("trade_no");
                billMapper.updateByPrimaryKeySelective(bill);

                //钱包金额变动。
                walletService.incr(bill.mchId, bill.money, bill.payCharge, bill.payType);
            }

            //加入异步通知下游商户系统
            //params jsonStr.
            if (!Strings.isNullOrEmpty(bill.notifyUrl)) {
                String params1 = payService.generateRes(
                        bill.money.toString(),
                        bill.mchOrderId,
                        bill.sysOrderId,
                        bill.status == Bill.FINISH ? "S" : bill.status == Bill.FAIL ? "F" : "I",
                        bill.tradeTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(bill.tradeTime),
                        bill.reserve);

                payClient.sendNotify(bill.id, bill.notifyUrl, params1, true);
            }
            writer.write("success");
        } else {
            writer.write("sign error");
        }
    }

    public String signCheck(String orderNo, String amount, String receiptAmount, String payTime, String tradeNo) throws Exception {
        String sysStr = appid + "create";
        String notifyStr = "order_no" + orderNo + "amount" + amount + "receipt_amount" + receiptAmount + "pay_time" + payTime + "trade_no" + tradeNo;
        return SignUtil.MD5(sysStr + notifyStr + token);
    }
}
