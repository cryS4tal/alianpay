package com.ylli.api.third.pay.service;

import com.google.gson.Gson;
import com.ylli.api.pay.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class CntClient {
    @Value("${pay.cnt.notify}")
    public String notifyUrl;
    @Value("${pay.cnt.appid}")
    public String appId;
    @Value("${pay.cnt.uid}")
    public String userId;
    @Value("${pay.cnt.secret}")
    public String secret;
    @Value("${pay.cont.success_code}")
    public String successCode;

    @Autowired
    RestTemplate restTemplate;

    public String createCntOrder(String sysOrderId, String mchId, String mz, String payType) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("userId", userId);
        params.add("merchantUserID", mchId);
        params.add("userOrder", sysOrderId);
        params.add("number", mz);
        params.add("payType", payType);
        params.add("isPur", "1");
        params.add("remark", mchId);
        params.add("appID", appId);
        params.add("ckValue", generateCkValue(userId, mchId, sysOrderId, mz, payType, "1", mchId, appId));
        System.out.println(new Gson().toJson(params));
        return post(params, "https://cntpay.io/trade/placeOrder");
    }

    public String post(MultiValueMap<String, String> params, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        System.out.println(new Gson().toJson(params));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        System.out.println(response.getBody());
        return response.getBody();
    }

    /**
     * 确认下单
     *
     * @param orderId
     * @param cardId
     * @return
     * @throws Exception
     */
    public String confirm(String orderId, String cardId) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("userId", userId);
        params.add("appID", appId);
        params.add("orderId", orderId);
        params.add("cardId", cardId);
        params.add("ckValue", generateCkValue(userId, orderId, cardId, appId));
        return post(params, "https://cntpay.io/trade/payment");
    }

    /**
     * 取消订单
     */
    public String cancel(String orderid, String mchId) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("userId", userId);
        params.add("merchantUserID", mchId);
        params.add("orderId", orderid);
        params.add("appID", appId);
        params.add("ckValue", generateCkValue(userId, mchId, orderid, appId));
        return post(params, "https://cntpay.io/trade/orderCancel");

    }

    public String generateCkValue(String... arr) throws Exception {
        StringBuffer sb = new StringBuffer();
        for (String s : arr) {
            sb.append(s).append("|");
        }
        sb.append(secret);
        System.out.println(sb.toString());
        return SignUtil.MD5(sb.toString()).toLowerCase();
    }


}
