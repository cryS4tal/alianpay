package com.ylli.api.third.pay.service.deprecate;

import com.google.gson.Gson;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.third.pay.modelVo.deprecate.WzQueryRes;
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

    @Value("${pay.wz.df.id}")
    public String spid_cash;

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

    /**
     * 订单查询
     */
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

    /**
     * @param account
     * @param card
     * @param name
     * @param fullname
     * @param linked
     * @param money
     * @param cityid
     * @param idcard
     * @param mobile
     * @param sysOrderId 系统生成唯一订单号
     * @return
     * @throws Exception
     */
    public String cash(String account, String card, String name, String fullname, String linked, String money,
                       String cityid, String idcard, String mobile, String sysOrderId) throws Exception {
        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "http://nfo.cdwzwl.com/createorder/payorder")
                .queryParam("account", account)
                .queryParam("card", card)
                .queryParam("name", name)
                .queryParam("fullname", fullname)
                .queryParam("linked", linked)
                .queryParam("spid", spid_cash)
                .queryParam("order", sysOrderId)
                .queryParam("type", 1)
                .queryParam("money", money)
                .queryParam("cityid", cityid)
                .queryParam("idcard", idcard)
                .queryParam("mobile", mobile)
                .queryParam("sign", generateSign(money, account, card, name, fullname, linked, sysOrderId))
                .build().toUriString();
        try {
            String result = restTemplate.getForObject(requestUrl, String.class);

            return result;
        } catch (Exception ex) {
            ex.getMessage();
        }
        return null;
    }

    public String generateSign(String money, String account, String card, String name, String fullname, String linked, String sysOrderId) throws Exception {
        StringBuffer sb = new StringBuffer().append(money).append(account).append(card).append(name)
                .append(fullname).append(linked).append(spid_cash).append(sysOrderId).append(1).append(secret);
        return SignUtil.MD5(sb.toString()).toLowerCase();
    }

    public String cashRes(String sysOrderId) throws Exception {
        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "http://nfc.cdwzwl.com/queryorder/df_query")
                .queryParam("spid", spid)
                .queryParam("sporder", sysOrderId)
                .queryParam("sign", testSign1(sysOrderId))
                .build().toUriString();
        String result = null;
        try {
            result = restTemplate.getForObject(requestUrl, String.class);

            return result;
        } catch (Exception ex) {
            ex.getMessage();
        }
        return result;
    }

    public String testSign1(String sysOrderId) throws Exception {
        StringBuffer sb = new StringBuffer().append(spid).append(sysOrderId).append(secret);
        return SignUtil.MD5(sb.toString());
    }
}
