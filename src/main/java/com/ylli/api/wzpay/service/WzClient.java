package com.ylli.api.wzpay.service;

import com.google.gson.Gson;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.wzpay.model.WzQueryRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WzClient {

    @Value("${pay.wz.notify}")
    public String notifyUrl;

    @Value("${pay.wz.id}")
    public String spid;

    @Value("${pay.wz.secret}")
    public String secret;

    @Autowired
    RestTemplate restTemplate;

    /**
     * @param orderid     商户订单号
     * @param mz          订单金额：单位为元，可支持两位小数点
     * @param spzdy       用户自定义（非汉字，可以为空）
     * @param uid         用户ID
     * @param spsuc       返回商户页面
     * @param productname 商品名称
     *                    banktype    银行卡类型:1，借记卡；2，信用卡
     *                    ordertype     支付类型（1、支付宝/2、微信；3、网银支付；4、QQ支付； 5、快捷支付；6、京东钱包；7、银联钱包；8、聚合支付）
     *                    interfacetype 接口类型（1：扫码； 3：App；4：WAP；5：服务窗；6：直连）
     *                    sign
     *                    暂时只支持 支付宝H5 ordertype=1 interfacetype=4
     */
    public String createWzOrder(String orderid, String mz, String spzdy, String uid, String spsuc, String productname) throws Exception {

        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "http://nfo.cdwzwl.com/createorder/index")
                .queryParam("spid", spid)
                .queryParam("orderid", orderid)
                .queryParam("mz", mz)
                .queryParam("spzdy", spzdy)
                .queryParam("uid", uid)
                .queryParam("spsuc", spsuc)
                .queryParam("ordertype", 1)
                .queryParam("interfacetype", 4)
                .queryParam("productname", productname)
                .queryParam("sign", generateSign(orderid, mz, spsuc))
                .queryParam("notifyurl", notifyUrl)
                .queryParam("banktype", 1)
                .build()
                .toUriString();
        return requestUrl;
    }

    public String generateSign(String orderid, String mz, String spsuc) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(spid).append(orderid).append(secret).append(mz).append(spsuc).append(1).append(4);
        return SignUtil.MD5(sb.toString());
    }


    public WzQueryRes orderQuery(String sysOrderId) throws Exception {

        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "http://nfc.cdwzwl.com/QueryOrder/Index")
                .queryParam("spid", spid)
                .queryParam("orderid", sysOrderId)
                .queryParam("sign", generateSign(sysOrderId))
                .build().encode().toUriString();
        try {
            String result = restTemplate.getForObject(requestUrl, String.class);
            return new Gson().fromJson(result, WzQueryRes.class);
        } catch (Exception ex) {
            ex.getMessage();
        }
        return null;
    }

    public String generateSign(String orderid) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(spid).append(orderid).append(secret);
        return SignUtil.MD5(sb.toString());
    }
}
