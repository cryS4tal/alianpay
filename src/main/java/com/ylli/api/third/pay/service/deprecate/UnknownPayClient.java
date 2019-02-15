package com.ylli.api.third.pay.service.deprecate;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.modelVo.deprecate.UnknownOrderRes;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UnknownPayClient {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UnknownPayClient.class);

    @Value("${pay.123.uid}")
    public String uid;

    @Value("${pay.123.token}")
    public String token;

    @Value("${pay.123.notify}")
    public String notifyUrl;

    @Autowired
    RestTemplate restTemplate;


    public String createOrder(String price, Integer istype, String redirectUrl, String sysOrderId, String uuid, int isJson) throws Exception {

        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "https://gateway.iexindex.top")
                .queryParam("uid", uid)
                .queryParam("price", price)
                .queryParam("istype", istype)
                .queryParam("notify_url", notifyUrl)
                .queryParam("return_url", redirectUrl)
                .queryParam("orderid", sysOrderId)
                .queryParam("codeid", uuid)
                .queryParam("json", isJson)
                .queryParam("key", generateSign(uuid, istype, notifyUrl, sysOrderId, price, redirectUrl))
                .build().toUriString();
        String result = restTemplate.getForObject(requestUrl, String.class);

        if (!Strings.isNullOrEmpty(result)) {
            // isJson = 1对应解析，isJson = 2 直接返回form.
            if (isJson == 2) {
                return result;
            } else {
                try {
                    UnknownOrderRes res = new Gson().fromJson(result, UnknownOrderRes.class);
                    //sign 校验
                    if (checkOrderSign(res)) {
                        return res.payurl;
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return null;
    }

    public String generateSign(String uuid, Integer istype, String notifyUrl, String sysOrderId, String price, String redirectUrl) throws Exception {
        StringBuffer sb = new StringBuffer().append(uuid).append(istype).append(notifyUrl).append(sysOrderId).append(price).append(redirectUrl).append(token).append(uid);

        return SignUtil.MD5(sb.toString()).toLowerCase();
    }

    public Boolean checkOrderSign(UnknownOrderRes res) throws Exception {
        return res.key.toLowerCase().equals(SignUtil.MD5(
                new StringBuffer().append(res.istype)
                        .append(res.payurl)
                        .append(res.price)
                        .append(token)
                        .toString()).toLowerCase());
    }
}
