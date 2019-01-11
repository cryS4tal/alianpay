package com.ylli.api.third.pay.service;

import com.ylli.api.pay.model.Bill;
import com.ylli.api.pay.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EazyPayService {

    @Autowired
    EazyClient eazyClient;

    @Autowired
    BillService billService;

    @Transactional
    public String createOrder(Long mchId, Long channelId, Integer money, String mchOrderId, String notifyUrl, String redirectUrl,
                              String reserve, String payType, String tradeType, Object extra) {

        //创建订单
        Bill bill = billService.createBill(mchId, mchOrderId, channelId, payType, tradeType, money, reserve, notifyUrl, redirectUrl);

        String result = eazyClient.createOrder();

        return null;
    }
}
