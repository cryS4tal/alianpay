package com.ylli.api.third.pay.service.guagua;

import com.alibaba.fastjson.JSON;
import com.ylli.api.third.pay.util.AESTool;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class GuaGuaClient {

    /*机构编号 */
    String orgNo = "190107229948831";

    /*商户编号*/
    String merNo = "101901080099121";

    /*请求Action*/
    String action = "AlipayH5Order";

    /*密钥*/
    String req_key = "839bb65af9fe4e604500cb7e3dc61b27";

    @Autowired
    RestTemplate restTemplate;

    /**
     * @param sysOrderId
     * @param amount
     */
    public String createOrder(String sysOrderId, String amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        /*加密data处理*/
        JSONObject dataParms = new JSONObject();
        dataParms.put("linkId", sysOrderId);
        dataParms.put("orderType", "10");
        dataParms.put("goodsName", "goodsName");
        dataParms.put("amount", amount);
        dataParms.put("notifyUrl", "notifyUrl");
        String dataStr = AESTool.encrypt(dataParms.toString(), req_key.substring(0, 16));

        /*签名数据*/
        StringBuilder signBuffer = new StringBuilder();
        signBuffer.append(orgNo);
        signBuffer.append(merNo);
        signBuffer.append(action);
        signBuffer.append(dataStr);
        /*MD5生成签名*/
        String sign = sign(signBuffer.toString(), req_key);

        //提交参数设置
        MultiValueMap<String, String> p = new LinkedMultiValueMap<>();
        p.add("orgNo", orgNo);
        p.add("merNo", merNo);
        p.add("action", action);
        p.add("data", dataStr);
        p.add("sign", sign);

        //提交请求
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(p, headers);

        String result = restTemplate.postForObject("http://www.guaguadata.com:7000/sdk/action", entity, String.class);

        com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result.toString());
        String rtnData = jsonObject.getString("data");
        if (rtnData != null) {
            System.out.println("－－－－－－返回数据明文－－－－－－－");
            System.out.println(AESTool.decrypt(rtnData, req_key.substring(0, 16)));
        }
        return result;
    }

    public String sign(String sign, String key) {
        return toHexString(md5((sign + key).getBytes()));
    }

    private static final String HEX_CHARS = "0123456789abcdef";

    public static String toHexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_CHARS.charAt(b[i] >>> 4 & 0x0F));
            sb.append(HEX_CHARS.charAt(b[i] & 0x0F));
        }
        return sb.toString();
    }

    public byte[] md5(byte[] data) {
        try {
            return MessageDigest.getInstance("MD5").digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
