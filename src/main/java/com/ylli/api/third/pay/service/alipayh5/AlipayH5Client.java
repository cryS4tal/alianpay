package com.ylli.api.third.pay.service.alipayh5;

import com.ylli.api.pay.util.SignUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
public class AlipayH5Client {

    @Value("${alipayh5.appid}")
    private String appid;

    @Value("${alipayh5.secret}")
    private String secret;

    @Value("${alipayh5.token}")
    private String token;

    @Value("${alipayh5.notify}")
    private String notifyUrl;

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
        /* 系统参数 */
        map.add("appid", appid);
        map.add("secret", secret);
        //接口版本，固定传1.0
        map.add("ver", "1.0");
        //请求接口名，固定create
        map.add("method", "create");
        map.add("ts", String.valueOf(System.currentTimeMillis() / 1000));

        /* 业务参数 */
        map.add("order_no", sysOrderId);
        map.add("amount", price);
        //WAP & QRCODE:返回用于生成二维码的URL地址
        map.add("type", "wap");
        map.add("notify_url", notifyUrl);
        map.add("goodsname", goodsname());
        //支付宝二维码
        map.add("channel", "100");

        map.add("sign", generateSignature(map));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange("http://47.99.135.8:8018/api/gateway",
                    HttpMethod.POST, new HttpEntity<Object>(map, headers), String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response.getBody();
    }

    private String generateSignature(MultiValueMap<String, String> map) throws Exception {
        String sysStr = map.getFirst("appid") + map.getFirst("method") + map.getFirst("ts") + map.getFirst("ver");
        String orderStr = "order_no" + map.getFirst("order_no") + "amount" + map.getFirst("amount") + "type" + map.getFirst("type") + "notify_url" + map.getFirst("notify_url");

        System.out.println(sysStr + orderStr + token);
        System.out.println(SignUtil.MD5(sysStr + orderStr + token));
        return SignUtil.MD5(sysStr + orderStr + token);
    }

    //随机生成商品名
    public String goodsname() {
        List<String> list = new ArrayList<String>() {
            {
                add("销售订单(宝胜浙江胜道)");
                add("绯村服饰店");
                add("半山小馆");
                add("杭州海茵汇酒店有限公司");
                add("商品");
                add("消费-交通出行");
                add("消费-生活日用");
            }
        };
        return list.get(new Random().nextInt(7));
    }
}
