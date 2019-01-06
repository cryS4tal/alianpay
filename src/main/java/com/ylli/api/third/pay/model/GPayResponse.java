package com.ylli.api.third.pay.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import static com.ucf.sdk.util.RSAUtils.KEY_ALGORITHM;
import com.ylli.api.pay.util.SignUtil;
import java.lang.reflect.Field;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

public class GPayResponse {
    public Integer code;
    public String message;
    public String sign;

    public GPOrder data;

    public static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMcM9dpvi3IKYYhQf1BLmW1vDcU5mOywAjT/eGgSvASOAcnO/ePGirwaVbOgdlOwISyaCQlnlhHISjEvg6g/SzrT5Pc9X9o2gvw5hsP5W584X2Vi5ZNF6jEPCYB/8ZlNnd3TlXPN23bJ056CEhF6vs0C/R7bdRaecBcQLtY4iyMwIDAQAB";

    public static void main(String[] args) {
        String str = "{\"code\":1,\"data\":{\"actualMoney\":0,\"aliName\":\"范明旺\",\"aliUserId\":\"2088432125984343\",\"bizCode\":\"\",\"channel\":\"alipay\",\"createdTime\":\"2019-01-06 16:25:22\",\"deviceId\":\"\",\"endtime\":null,\"id\":640121,\"matchId\":76,\"money\":100,\"notifyUrl\":\"http://t\",\"orderId\":\"76X1370X640121X88DJm\",\"orderNo\":\"\",\"payName\":\"\",\"payType\":\"H5\",\"payUrl\":\"http://api.gpayroot.com/pay.html?orderId=76X1370X640121X88DJm\",\"status\":\"SUCCESS\",\"terminalId\":1370,\"updatedTime\":\"2019-01-06 16:25:22\"},\"message\":\"下单成功\",\"sign\":\"FwexB9sS9knVxzmh/DsTIdnAdRi8IzFhpqy9y+nejkVPVykz1k58x25b1z/Kj7xagRF/OZ2lrlPkC9JQGtPtkg8VzQF96dU/E+6Qru1PK4IzZGwxClTD/bDT+MGdjIuVGOlLzFse6q6Z73DNSJBvbNVR0541XfoR525VwZX48Y4=\"}";

        GPayResponse response = new Gson().fromJson(str, GPayResponse.class);

        Boolean flag = checksign(publicKey, getSignature(objectToMap(response)), "FwexB9sS9knVxzmh/DsTIdnAdRi8IzFhpqy9y+nejkVPVykz1k58x25b1z/Kj7xagRF/OZ2lrlPkC9JQGtPtkg8VzQF96dU/E+6Qru1PK4IzZGwxClTD/bDT+MGdjIuVGOlLzFse6q6Z73DNSJBvbNVR0541XfoR525VwZX48Y4=");


        //String json =   ReadFromFile.read("D:\\json.txt","GBK");

        JSONObject jsonObject =  JSON.parseObject(str);

        String data =   jsonObject.getString("data");

        Map<String,Object> map =  JSON.parseObject(data);

        String sign = jsonObject.getString("sign");

        Boolean success = verifyMap(map,sign);

        System.out.println(success);
    }

    public static Boolean verifyMap(Map map, String sign) {
        Boolean success = false;
        try {
            if (!map.isEmpty()) {
                Set keySet = map.keySet();
                String[] keyArray = (String[]) keySet.toArray(new String[keySet.size()]);
                Arrays.sort(keyArray);
                StringBuffer buffer = new StringBuffer();
                for (String key : keyArray) {
                    /**
                     * 如果为空就不加入签名
                     */
                    String value = map.get(key) == null ? "" : map.get(key).toString();
                    if (StringUtils.isNotBlank(value)) {
                        buffer.append(key + "=" + value).append("&");
                    }
                }
                /**
                 * 重新排序后的字符串
                 */
                String str = buffer.toString().toUpperCase();
                success = verify(str.getBytes(), publicKey, sign);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public static boolean verify(byte[] data, String publicKey, String sign)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(Base64Utils.decode(sign.getBytes()));
    }

    public static Map objectToMap(Object object) {
        Map<String, String> map = new LinkedHashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field entityField : fields) {
            Field field = null;
            try {
                field = object.getClass().getDeclaredField(entityField.getName());
                map.put(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()), field.get(object) == null ? "" : field.get(object).toString());
            } catch (NoSuchFieldException exception) {
                exception.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 签名验证
     *
     * @param pubkeyvalue：公钥
     * @param oid_str：源串
     * @param signed_str：签名结果串
     * @return
     */
    public static boolean checksign(String pubkeyvalue, String oid_str,
                                    String signed_str) {
        try {
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(
                    getBytesBASE64(pubkeyvalue));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(bobPubKeySpec);
            byte[] signed = getBytesBASE64(signed_str);// 这是SignatureData输出的数字签名
            java.security.Signature signetcheck = java.security.Signature
                    .getInstance("MD5withRSA");
            signetcheck.initVerify(pubKey);
            signetcheck.update(oid_str.getBytes("GBK"));
            return signetcheck.verify(signed);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static byte[] getBytesBASE64(String s) {
        if (s == null)
            return null;
        //BASE64Decoder decoder = new BASE64Decoder();
        try {
            //byte[] b = decoder.decodeBuffer(s);
            byte[] b = Base64.decodeBase64(s);
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getSignature(Map<String, String> map) {
        //sort
        Set<String> keySet = map.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        //sign
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (Strings.isNullOrEmpty(map.get(k))) {
                continue;
            }
            if (map.get(k).trim().length() > 0) {// 参数值为空，则不参与签名
                sb.append(k).append("=").append(map.get(k).trim()).append("&");
            }
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
