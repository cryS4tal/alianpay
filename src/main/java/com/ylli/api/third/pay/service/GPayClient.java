package com.ylli.api.third.pay.service;

import com.google.gson.Gson;
import com.ylli.api.third.pay.model.GPayOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GPayClient {

    @Value("${gpay.mchid}")
    public String mchId;

    @Value("${gpay.secret}")
    public String secret;

    @Value("${gpay.notify.url}")
    public String notifyUrl;

    public static final String QR = "QR";
    public static final String H5 = "H5";

    public static final String ALIPAY = "alipay";
    public static final String WX = "wechat";

    @Autowired
    RestTemplate restTemplate;

    /**
     * @param money   金额(分)
     * @param channel 收款渠道 wechat微信 alipay 支付宝
     *                H5 仅支持支付宝 QR 扫码(微信只支持扫码)
     */
    public String createOrder(Integer money, String channel) {

        GPayOrder order = new GPayOrder();
        order.matchId = mchId;
        order.secret = secret;
        order.notifyUrl = notifyUrl;
        order.money = money.toString();
        order.channel = channel.equals(ALIPAY) ? ALIPAY : WX;
        order.payType = channel.equals(ALIPAY) ? H5 : QR;

        String toJson = new Gson().toJson(order);

        String result = restTemplate.postForObject("http://api.gpayroot.com/order/unify_order", toJson, String.class);

        return result;
    }
}
