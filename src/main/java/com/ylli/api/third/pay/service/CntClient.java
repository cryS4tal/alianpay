package com.ylli.api.third.pay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.phone.service.JpushSmsClient;
import com.ylli.api.third.pay.model.ConfirmReq;
import com.ylli.api.third.pay.model.UnknownOrderRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("userId", userId);
        params.add("merchantUserID", mchId);
        params.add("userOrder", sysOrderId);
        params.add("number", mz);
        params.add("payType", payType);
        params.add("isPur", "1");
        params.add("remark", mchId);
        params.add("appID", appId);
        params.add("ckValue", generateSign(userId, mchId, sysOrderId, mz, payType, "1", mchId, appId, secret));
        System.out.println(new Gson().toJson(params));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange("https://cntpay.io/trade/placeOrder", HttpMethod.POST, requestEntity, String.class);
       /* String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "https://gateway.iexindex.top")
                .queryParam("userId", userId)
                .queryParam("merchantUserID", mchId)
                .queryParam("userOrder", sysOrderId)
                .queryParam("number", mz)
                .queryParam("payType", payType)
                .queryParam("isPur", "1")
                .queryParam("remark", mchId)
                .queryParam("appID", appId)
                .queryParam("ckValue", generateSign(userId, mchId, sysOrderId, mz, payType, "1", mchId, appId, secret))
                .build().toUriString();
        System.out.println(requestUrl);
        String result = restTemplate.getForObject(requestUrl, String.class);
        System.out.println(result);*/
        return "";
    }

    public String confirm(String orderId){
        ConfirmReq req=new ConfirmReq();
        req.appID=appId;
        req.userId=userId;
        req.orderId=orderId;
        String s = restTemplate.postForObject("https://cntpay.io/trade/payment", req, String.class);
        System.out.println(s);
        return null;
    }

    private String generateSign(String userId, String merchantUserID, String userOrder, String number, String payType, String isPur, String remark, String appId, String secret) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(userId).append("|").append(merchantUserID).append("|").append(userOrder).append("|").append(number).append("|").append(payType).append("|").append(isPur).append("|").append(remark).append("|").append(appId).append("|").append(secret);
        System.out.println(sb.toString());
        return SignUtil.MD5(sb.toString()).toLowerCase();
    }
}
