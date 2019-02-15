package com.ylli.api.third.pay.service.gpay;

import com.google.gson.Gson;
import com.ylli.api.third.pay.modelVo.gpay.GPayOrder;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

    private RestTemplate restTemplate;

    @PostConstruct
    private void initRestTemplate() {
        restTemplate = new RestTemplate();
        setTimeout(restTemplate);
    }

    private void setTimeout(RestTemplate restTemplate) {
        if (restTemplate.getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(2 * 1000);
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(2 * 1000);
        } else if (restTemplate.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(2 * 1000);
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(2 * 1000);
        }
    }

    //

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
