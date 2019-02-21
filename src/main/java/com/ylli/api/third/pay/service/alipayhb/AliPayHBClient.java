package com.ylli.api.third.pay.service.alipayhb;

import com.ylli.api.pay.util.SignUtil;
import java.util.Arrays;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class AliPayHBClient {

    @Value("${zhbhb.mch.id}")
    public String merchantId;

    @Value("${zfbhb.notify}")
    public String notifyUrl;

    @Value("${zfbhb.secret}")
    public String secret;

    private RestTemplate restTemplate;

    @PostConstruct
    private void initRestTemplate() {
        restTemplate = new RestTemplate();
        setTimeout(restTemplate);
    }

    private void setTimeout(RestTemplate restTemplate) {
        if (restTemplate.getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(1 * 1000);
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(1 * 1000);
        } else if (restTemplate.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(1 * 1000);
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(1 * 1000);
        }
    }


    public String createOrder(String sysOrderId, String price) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        //提交参数设置
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("merchantId", merchantId);
        map.add("outTradeNo", sysOrderId);
        map.add("orderAmt", price);
        map.add("productName", "productName");
        map.add("notifyUrl", notifyUrl);
        map.add("payType", "AliHongbao");
        map.add("timeStamp", String.valueOf(System.currentTimeMillis()));

        map.add("sign", generateSignature(map, secret));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange("http://139.196.211.10/AbcPay/Pay.aspx",
                    HttpMethod.POST, new HttpEntity<Object>(map, headers), String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response.getBody();
    }

    public String generateSignature(final MultiValueMap<String, String> data, String key) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) {
                continue;
            }
            if (data.getFirst(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.getFirst(k)).append("&");
        }
        sb.append("key=").append(key);
        String sign = SignUtil.MD5(sb.toString()).toLowerCase();
        return sign;
    }
}
