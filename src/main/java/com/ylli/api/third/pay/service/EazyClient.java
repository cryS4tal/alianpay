package com.ylli.api.third.pay.service;

import com.ylli.api.third.pay.util.EazySignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class EazyClient {

    @Value("${pay.eazy.account.id}")
    public String accountId;

    @Value("${pay.eazy.notify}")
    public String notifyUrl;

    @Value("${pay.eazy.key}")
    public String KEY;

    @Autowired
    RestTemplate restTemplate;

    /**
     * @param sysOrderId
     * @param amount
     */
    public String createOrder(String sysOrderId, String amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        //提交参数设置
        MultiValueMap<String, String> p = new LinkedMultiValueMap<>();
        p.add("account_id", accountId);
        p.add("content_type", "json");
        //支付宝银行卡.alicard_auto
        p.add("thoroughfare", "alicard_auto");
        p.add("out_trade_no", sysOrderId);
        p.add("amount", amount);
        p.add("callback_url", notifyUrl);
        p.add("success_url", "success_url");
        p.add("error_url", "error_url");
        p.add("sign", EazySignUtil.encrypt(Double.valueOf(amount), sysOrderId, KEY));
        p.add("sign_type", "1");

        //提交请求
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(p, headers);

        String result = restTemplate.postForObject("http://zf.13wmb.cn/gateway/index/checkpoint.do", entity, String.class);

        return result;
    }


}
