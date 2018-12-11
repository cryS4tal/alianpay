package com.ylli.api.wzpay.service;

import com.ylli.api.pay.util.SerializeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WzService {

    @Autowired
    WzClient wzClient;

    @Autowired
    SerializeUtil serializeUtil;

    @Transactional
    public void createOrder(Integer money, String reserve, String redirectUrl) throws Exception {

        String orderid = serializeUtil.generateOrderNo("wz", 1002L, 5L);
        String mz = String.format("%.2f", (money / 100.0));
        wzClient.createWzOrder(orderid, mz, reserve, "1002", redirectUrl, "测试支付", "http://47.99.180.135:8088/pay/wz/notify");

    }

    /*public static void main(String[] args) {
        Integer money = 100;
        String a =
        System.out.println(a);
    }*/
}
