package com.ylli.api.wzpay.service;

import com.google.common.base.Strings;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.pay.util.SignUtil;
import com.ylli.api.wzpay.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WzClient {

    public String spid = "8047";

    public String secret = "9d0d30c49a8d446aa2";

    @Autowired
    RestTemplate restTemplate;

    /**
     * @param orderid     商户订单号
     * @param mz          订单金额：单位为元，可支持两位小数点
     * @param spzdy       用户自定义（非汉字，可以为空）
     * @param uid         用户ID
     * @param spsuc       返回商户页面
     * @param productname 商品名称
     * @param notifyurl   <p>
     *                    banktype    银行卡类型:1，借记卡；2，信用卡
     *                    ordertype     支付类型（1、支付宝/2、微信；3、网银支付；4、QQ支付； 5、快捷支付；6、京东钱包；7、银联钱包；8、聚合支付）
     *                    interfacetype 接口类型（1：扫码； 3：App；4：WAP；5：服务窗；6：直连）
     *                    sign
     *                    暂时只支持 支付宝H5 ordertype=1 interfacetype=4
     *                    </>
     */
    public void createWzOrder(String orderid, String mz, String spzdy, String uid, String spsuc, String productname, String notifyurl) throws Exception {

        if (Strings.isNullOrEmpty(orderid)) {
            throw new AwesomeException(Config.ERROR_INVALID_PARAM.format("订单号不能为空"));
        }
        if (Strings.isNullOrEmpty(mz)) {
            throw new AwesomeException(Config.ERROR_INVALID_PARAM.format("金额不能为空"));
        }
        if (Strings.isNullOrEmpty(uid)) {
            throw new AwesomeException(Config.ERROR_INVALID_PARAM.format("用户id不能为空"));
        }
        if (Strings.isNullOrEmpty(spsuc)) {
            throw new AwesomeException(Config.ERROR_INVALID_PARAM.format("spsuc前端跳转地址不能为空"));
        }
        if (Strings.isNullOrEmpty(productname)) {
            throw new AwesomeException(Config.ERROR_INVALID_PARAM.format("商品名称不能为空"));
        }
        if (Strings.isNullOrEmpty(notifyurl)) {
            throw new AwesomeException(Config.ERROR_INVALID_PARAM.format("回调地址不能为空"));
        }

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
                .queryParam("notifyurl", notifyurl)
                .queryParam("banktype", 1)
                .build()
                //.encode()
                .toUriString();
        /*try {
            restTemplate.getForObject(requestUrl, Object.class);
        } catch (Exception ex) {
            ex.getMessage();
        }*/
        System.out.println(requestUrl);
    }

    public String generateSign(String orderid, String mz, String spsuc) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append(spid).append(orderid).append(secret).append(mz).append(spsuc).append(1).append(4);
        String test = sb.toString();

        String md5 = SignUtil.MD5(sb.toString());
        return SignUtil.MD5(sb.toString());
    }
}
