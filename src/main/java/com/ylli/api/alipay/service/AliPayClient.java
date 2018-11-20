package com.ylli.api.alipay.service;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.alipay.mapper.NotifyMapper;
import com.ylli.api.alipay.model.Notify;
import com.ylli.api.alipay.model.OrderNotify;
import com.ylli.api.alipay.model.OrderNotifyRes;
import com.ylli.api.alipay.model.OrderQueryRequest;
import com.ylli.api.alipay.model.OrderQueryResponse;
import com.ylli.api.alipay.model.OrderRequest;
import com.ylli.api.alipay.model.OrderResponse;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class AliPayClient {

    //商户号
    @Value("${ky.pay.mchid}")
    public Long mchId;

    //应用ID
    @Value("${ky.pay.appid}")
    public String appId;

    @Value("${ky.pay.private_key}")
    private String privateKey;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    NotifyMapper notifyMapper;

    /**
     * 封装快易支付统一下单接口
     *
     * @return
     */
    public OrderResponse createAliPayOrder() {

        Gson gson = new Gson();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        OrderRequest request = new OrderRequest(mchId, appId,
                8007, getOrderNo(1001L),
                "cny", 100, null, null, "returnUrl",
                "notifyUrl", "测试商品主题",
                "测试商品描述", null, null, null);

        request.sign = getOrderSign(request);

        //提交参数设置
        MultiValueMap<String, String> p = new LinkedMultiValueMap<>();
        p.add("params", gson.toJson(request));

        //提交请求
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(p, headers);

        String result = restTemplate.postForObject("http://p.kypay.fjklt.net/api/pay/create_order", entity, String.class);

        return gson.fromJson(result, OrderResponse.class);
    }

    /**
     * 支付查询接口
     */
    public OrderQueryResponse aliPayOrderQuery() {

        Gson gson = new Gson();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        OrderQueryRequest request = new OrderQueryRequest(String.valueOf(mchId), appId, "", "201811200538291001");
        request.sign = getQuerySign(request);

        //提交参数设置
        MultiValueMap<String, String> p = new LinkedMultiValueMap<>();
        p.add("params", gson.toJson(request));

        //提交请求
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(p, headers);

        String result = restTemplate.postForObject("http://p.kypay.fjklt.net/api/pay/query_order", entity, String.class);

        return gson.fromJson(result, OrderQueryResponse.class);

    }

    /**
     * 向第三方发送回调通知
     *
     * @return
     */
    @Async
    @Transactional
    public OrderNotifyRes sendNotify(Notify notify) {
        if (notify.failCount > 5) {
            notifyMapper.delete(notify);
            return new OrderNotifyRes("FAIL","失败次数上限");
        }
        String res = restTemplate.postForObject(notify.url, notify.params, String.class);
        OrderNotifyRes notifyRes = new Gson().fromJson(res, OrderNotifyRes.class);

        if (!notifyRes.code.toUpperCase().equals("SUCCESS")) {
            notify.failCount = notify.failCount + 1;
            notify.modifyTime = Timestamp.from(Instant.now());
            notifyMapper.updateByPrimaryKeySelective(notify);
        } else {
            notifyMapper.delete(notify);
        }
        return notifyRes;
    }

    public String getOrderSign(OrderRequest request) {
        //添加信息
        Map<String, String> map = new HashMap<>();
        map.put("appId", request.appId);
        map.put("mchId", String.valueOf(request.mchId));
        map.put("productId", String.valueOf(request.productId));
        map.put("mchOrderNo", request.mchOrderNo);
        map.put("currency", request.currency);
        map.put("amount", String.valueOf(request.amount));
        map.put("clientIp", request.clientIp);
        map.put("device", request.device);
        map.put("returnUrl", request.returnUrl);
        map.put("notifyUrl", request.notifyUrl);
        map.put("subject", request.subject);
        map.put("body", request.body);
        map.put("param1", request.param1);
        map.put("param2", request.param2);
        map.put("extra", request.extra);
        try {
            return getSignature(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getQuerySign(OrderQueryRequest request) {
        //添加信息
        Map<String, String> map = new HashMap<>();
        map.put("appId", request.appId);
        map.put("mchId", String.valueOf(request.mchId));
        map.put("payOrderId", request.payOrderId);
        map.put("mchOrderNo", request.mchOrderNo);
        map.put("executeNotify", String.valueOf(request.executeNotify));

        try {
            return getSignature(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 签名算法
     */
    public String getSignature(Map<String, String> map) throws Exception {
        //sort
        Set<String> keySet = map.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        //sign
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            //sign不参与签名
            if (Strings.isNullOrEmpty(map.get(k)) || k.equals("sign")) {
                continue;
            }
            // 参数值为空，则不参与签名
            if (map.get(k).trim().length() > 0) {
                sb.append(k).append("=").append(map.get(k).trim()).append("&");
            }
        }
        sb.append("key=").append(privateKey);

        return MD5(sb.toString()).toUpperCase();
    }

    /**
     * 生成MD5
     */
    public static String MD5(String data) throws Exception {
        java.security.MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    /**
     * 生成订单号
     */
    public String getOrderNo(Long id) {
        StringBuffer sb = new StringBuffer();
        sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(Date.from(Instant.now())));
        sb.append(id);
        return sb.toString();
    }

    /**
     * 签名验证
     */
    public boolean isSignatureValid(String jsonStr) throws Exception {
        Map<String, String> data = jsonToMap(jsonStr);
        String sign = data.get("sign");
        if (Strings.isNullOrEmpty(sign)) {
            return false;
        }
        return getSignature(data).equals(sign);
    }

    private Map<String, String> jsonToMap(String jsonStr) {
        OrderNotify notify = new Gson().fromJson(jsonStr, OrderNotify.class);
        //添加信息
        Map<String, String> map = new HashMap<>();
        map.put("payOrderId", notify.payOrderId);
        map.put("mchId", notify.mchId);
        map.put("appId", notify.appId);
        map.put("productId", String.valueOf(notify.productId));
        map.put("mchOrderNo", notify.mchOrderNo);
        map.put("amount", String.valueOf(notify.amount));
        map.put("status", String.valueOf(notify.status));
        map.put("channelOrderNo", notify.channelOrderNo);
        map.put("channelAttach", notify.channelAttach);
        map.put("param1", notify.param1);
        map.put("param2", notify.param2);
        map.put("paySuccTime", String.valueOf(notify.paySuccTime));
        map.put("backType", String.valueOf(notify.backType));
        map.put("sign", notify.sign);
        return map;
    }
}
