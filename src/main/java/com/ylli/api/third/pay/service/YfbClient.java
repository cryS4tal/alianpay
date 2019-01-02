package com.ylli.api.third.pay.service;

import com.ylli.api.pay.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class YfbClient {

    private static Logger LOGGER = LoggerFactory.getLogger(YfbClient.class);

    @Value("${yfb.mch.id}")
    public String parter;// = "3568";

    @Value("${yfb.secret}")
    public String secret;// = "002a3ebc1a8e4fb19e517aa3c7037262";

    @Autowired
    RestTemplate restTemplate;


    @Value("${yfb.notify.url}")
    public String notifyUrl;// = "http://116.62.209.131:8088/pay/yfb/notify";

    /**
     * @param type        银行类型
     * @param value       金额,单位元（人民币），2 位小数
     * @param orderid     商户订单号
     * @param callbackurl 下行异步通知地址
     * @param hrefbackurl 下行同步通知地址
     * @param payerIp     支付用户 IP
     * @param attach      备注消息
     */
    public String order(String type, String value, String orderid, String callbackurl, String hrefbackurl, String payerIp, String attach) throws Exception {

        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "http://api.qianyipay.com/chargebank.aspx")
                .queryParam("parter", parter)
                .queryParam("type", type)
                .queryParam("value", value)
                .queryParam("orderid", orderid)
                .queryParam("callbackurl", notifyUrl)
                .queryParam("hrefbackurl", hrefbackurl)
                .queryParam("payerIp", payerIp)
                .queryParam("attach", attach)
                .queryParam("sign", generateSign(parter, type, value, orderid, notifyUrl))
                .build().toUriString();

        try {

            String result = restTemplate.getForObject(requestUrl, String.class);
            //System.out.println(result);
            return result;
        } catch (RestClientResponseException ex) {
            ex.printStackTrace();
            LOGGER.error(ex.toString());
        }
        return null;
    }

    public String generateSign(String parter, String type, String value, String orderid, String callbackurl) throws Exception {
        StringBuffer sb = new StringBuffer()
                .append("parter=").append(parter)
                .append("&type=").append(type)
                .append("&value=").append(value)
                .append("&orderid=").append(orderid)
                .append("&callbackurl=").append(callbackurl)
                .append(secret);

        return SignUtil.MD5(sb.toString(), "GB2312").toLowerCase();
    }

    public boolean signVerify(String orderid, String opstate, String ovalue, String sign) throws Exception {
        StringBuffer sb = new StringBuffer()
                .append("orderid=").append(orderid)
                .append("&opstate=").append(opstate)
                .append("&ovalue=").append(ovalue)
                .append(secret);
        return SignUtil.MD5(sb.toString(), "GB2312").toLowerCase().equals(sign.toLowerCase());
    }

    public String orderQuery(String orderid) throws Exception {
        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "http://api.qianyipay.com/Search.aspx")
                .queryParam("orderid", orderid)
                .queryParam("parter", parter)
                .queryParam("sign", generateSign(parter, orderid))
                .build().toUriString();

        try {
            String result = restTemplate.getForObject(requestUrl, String.class);
            return result;
        } catch (RestClientResponseException ex) {
            ex.printStackTrace();
            LOGGER.error(ex.toString());
        }
        return null;
    }

    private String generateSign(String parter, String orderid) throws Exception {
        StringBuffer sb = new StringBuffer()
                .append("orderid=").append(orderid)
                .append("&parter=").append(parter)
                .append(secret);
        return SignUtil.MD5(sb.toString(), "GB2312").toLowerCase();
    }
}
