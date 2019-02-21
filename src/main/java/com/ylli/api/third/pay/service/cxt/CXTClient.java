package com.ylli.api.third.pay.service.cxt;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.modelVo.cxt.CXTOrder;
import com.ylli.api.third.pay.modelVo.cxt.CXTResponse;
import java.util.Arrays;
import java.util.Map;
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
import org.springframework.web.client.RestTemplate;

@Component
public class CXTClient {

    @Value("${cxt.uid}")
    public String uid;

    @Value("${cxt.token}")
    public String token;

    @Value("${cxt.notify}")
    public String notifyUrl;

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

    public CXTResponse createOrder(String sysOrderId, String price, String redirectUrl) throws Exception {

        CXTOrder order = new CXTOrder();
        order.uid = uid;
        order.orderid = sysOrderId;
        //10001支付宝,20001微信,30001 银联,40001 支付宝转银行卡
        order.istype = "40001";
        order.goodsname = "goodsname";
        order.price = price;
        //固定值为json
        order.format = "format";
        order.notifyUrl = notifyUrl;
        order.returnUrl = Strings.isNullOrEmpty(redirectUrl) ? "http://www.baidu.com" : redirectUrl;
        order.orderuid = "goodsname descript";
        order.key = generateKey(order);
        order.token = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        String str = new Gson().toJson(order);
        ResponseEntity<CXTResponse> response = null;
        try {
            response = restTemplate.exchange("http://payy.junded.cn/cxt/payment/subOrder",
                    HttpMethod.POST, new HttpEntity<Object>(str, headers), CXTResponse.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response.getBody();
    }

    public String generateKey(CXTOrder order) throws Exception {
        order.token = token;
        Map<String, String> data = SignUtil.objectToMap(order);
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("format") || k.equals("key")) {
                continue;
            }
            if (data.get(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(data.get(k));
        }
        System.out.println(sb.toString());
        return SignUtil.MD5(sb.toString()).toLowerCase();
    }
}
