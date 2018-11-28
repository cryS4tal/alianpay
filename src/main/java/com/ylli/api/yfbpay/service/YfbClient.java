package com.ylli.api.yfbpay.service;

import com.ylli.api.pay.util.SignUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class YfbClient {

    //@Value("")
    public String parter = "3568";

    //@Value("")
    public String secret = "002a3ebc1a8e4fb19e517aa3c7037262";

    @Autowired
    RestTemplate restTemplate;


    /**
     * @param type        银行类型
     * @param value       金额,单位元（人民币），2 位小数
     * @param orderid     商户订单号
     * @param callbackurl 下行异步通知地址
     * @param hrefbackurl 下行同步通知地址
     * @param payerIp     支付用户 IP
     * @param attach      备注消息
     */
    public void order(String type, String value, String orderid, String callbackurl, String hrefbackurl, String payerIp, String attach) throws Exception {

        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "http://api.qianyipay.com/chargebank.aspx")
                .queryParam("parter", parter)
                .queryParam("type", type)
                .queryParam("value", value)
                .queryParam("orderid", orderid)
                .queryParam("callbackurl", "http://47.99.180.135:8080/pay/yfb/notify")
                .queryParam("hrefbackurl", hrefbackurl)
                .queryParam("payerIp", payerIp)
                .queryParam("attach", attach)
                .queryParam("sign", generateSign(parter, type, value, orderid, callbackurl))
                .build().toUriString();

        try {

            String result = restTemplate.getForObject(requestUrl, String.class);
            System.out.println(result);
        } catch (RestClientResponseException ex) {
            ex.printStackTrace();
            //LOGGER.error(ex.toString());
            //throw new AwesomeException(Config.ERROR_VERIFY_SERVER);
        }
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


    public void payNotify(String orderid) {

    }
}
