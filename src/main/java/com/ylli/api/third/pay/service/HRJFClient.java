package com.ylli.api.third.pay.service;

import com.ylli.api.pay.util.SignUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class HRJFClient {

    private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HRJFClient.class);

    @Value("${pay.hrjf.parter}")
    public String parter;

    @Value("${pay.hrjf.notify}")
    public String notifyUrl;

    @Value("${pay.hrjf.token}")
    public String token;

    @Autowired
    RestTemplate restTemplate;

    public String createOrder(String value, String sysOrderId, String redirectUrl, String reserve, Long mchId) throws Exception {
        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "http://gateway.iexindex.com/quickpay/index.aspx")
                .queryParam("parter", parter)
                .queryParam("type", "6018")
                .queryParam("value", value)
                .queryParam("orderid", sysOrderId)
                .queryParam("callbackurl", notifyUrl)
                .queryParam("hrefbackurl", redirectUrl)
                .queryParam("attach", 1)
                .queryParam("payuserid", mchId)
                .queryParam("h5", 2)
                .queryParam("sign", generateSign(value, sysOrderId, mchId))
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

    public String generateSign(String value, String sysOrderId, Long mchId) throws Exception {
        StringBuffer sb = new StringBuffer()
                .append("parter=").append(parter)
                .append("&type=").append("6018")
                .append("&value=").append(value)
                .append("&orderid=").append(sysOrderId)
                .append("&callbackurl=").append(notifyUrl)
                .append("&payuserid=").append(mchId)
                .append("&attach=").append(1)
                .append(token);
        String str = sb.toString();
        return SignUtil.MD5(sb.toString()).toLowerCase();
    }

    public boolean signVerify(String orderid, String opstate, String ovalue, String sign) throws Exception {
        StringBuffer sb = new StringBuffer()
                .append("orderid=").append(orderid)
                .append("&opstate=").append(opstate)
                .append("&ovalue=").append(ovalue)
                .append(token);
        return SignUtil.MD5(sb.toString(), "GB2312").toLowerCase().equals(sign.toLowerCase());
    }
}
