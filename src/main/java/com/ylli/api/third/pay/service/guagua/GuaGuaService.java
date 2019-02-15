package com.ylli.api.third.pay.service.guagua;

import com.ylli.api.pay.mapper.BillMapper;
import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuaGuaService {

    @Autowired
    GuaGuaClient guaGuaClient;

    @Autowired
    BillService billService;

    @Autowired
    BillMapper billMapper;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl,
                              String reserve, String payType, String tradeType, Object extra) {

        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        //分转换元
        String amount = String.format("%.2f", (money / 100.0));

        String result = guaGuaClient.createOrder(bill.sysOrderId, amount);

        return result;
    }
}
