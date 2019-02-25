package com.ylli.api.third.pay.service.dh;

import com.ylli.api.pay.util.SignUtil;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
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
public class DHClient {

    @Value("${dh.member.id}")
    public String memberId;

    @Value("${dh.notify}")
    public String notifyUrl;

    @Value("${dh.sercret}")
    public String secret;

    private RestTemplate restTemplate;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        map.add("pay_memberid", memberId);
        map.add("pay_orderid", sysOrderId);
        map.add("pay_applydate", sdf.format(new Date()));
        //903-⽀付宝扫码,904-⽀付宝 H5
        map.add("pay_bankcode", "904");
        map.add("pay_notifyurl", notifyUrl);
        map.add("pay_callbackurl", notifyUrl);
        map.add("pay_amount", price);
        map.add("pay_productname", memberId);
        map.add("pay_md5sign", generateSignature(map, secret));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange("https://www.dh6788.com/Pay_Index.html",
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
            if (k.equals("pay_md5sign") || k.equals("pay_productname")) {
                continue;
            }
            //if (data.getFirst(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.getFirst(k)).append("&");
            //System.out.println(sb.toString());
        }
        sb.append("key=").append(key);
        System.out.println(sb.toString());

        String sign = SignUtil.MD5(sb.toString());

        System.out.println(sign);
        return sign;
    }
}
