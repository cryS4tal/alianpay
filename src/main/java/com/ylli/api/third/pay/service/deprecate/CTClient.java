package com.ylli.api.third.pay.service.deprecate;

import com.ylli.api.pay.util.SignUtil;
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
public class CTClient {

    @Value("${ct.mch.id}")
    public String mchId;

    @Value("${ct.notify.url}")
    public String notifyUrl;

    @Value("${ct.secret}")
    public String secret;

    @Autowired
    RestTemplate restTemplate;

    /**
     * @param totalFee   金额/元
     * @param sysOrderId 系统订单号 attach
     * @return
     */
    public String createOrder(String totalFee, String sysOrderId) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String ts = String.valueOf(System.currentTimeMillis());

        //提交参数设置
        MultiValueMap<String, String> p = new LinkedMultiValueMap<>();
        p.add("mch_id", mchId);
        p.add("terminal_time", ts);
        p.add("total_fee", totalFee);
        p.add("attach", sysOrderId);
        p.add("notify_url", notifyUrl);
        p.add("sign", generateSign(ts, totalFee, sysOrderId));

        //提交请求
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(p, headers);

        String result = restTemplate.postForObject("http://changtong.ppgpay.com/IPS/qpay.do?method=alipaySecret", entity, String.class);

        return result;
    }

    public String generateSign(String terminalTime, String totalFee, String attach) throws Exception {
        String tempStr = new StringBuffer()
                .append(mchId)
                .append(secret)
                .append(terminalTime)
                .append(totalFee)
                .append(attach)
                .append(notifyUrl).toString();
        return SignUtil.MD5(tempStr).toLowerCase();
    }
}
