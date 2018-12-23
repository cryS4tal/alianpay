package com.ylli.api.third.pay.service;

import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import static com.ylli.api.pay.service.PayService.NATIVE;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HRJFService {

    @Autowired
    BillService billService;

    @Autowired
    HRJFClient hrjfClient;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl, String reserve, String payType, String tradeType, Object extra) throws Exception {
        if (tradeType == null) {
            //支付方式. 默认扫码
            tradeType = NATIVE;
        }
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        String value = (new BigDecimal(money).divide(new BigDecimal(100))).toString();

        String str = hrjfClient.createOrder(value, bill.sysOrderId, redirectUrl, reserve, mchId);

        return str;

    }
}
