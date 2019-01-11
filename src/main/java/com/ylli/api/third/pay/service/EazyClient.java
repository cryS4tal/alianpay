package com.ylli.api.third.pay.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class EazyClient {

    DecimalFormat df = new DecimalFormat("#0.00");

    //@Value("${}")
    public String accountId;

    @Autowired
    RestTemplate restTemplate;

    public static final String KEY = "214F80B813C848";

    /**
     * @param payType
     * @param sysOrderId
     * @return
     */
    public String createOrder(String payType, String sysOrderId, String amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        //提交参数设置
        MultiValueMap<String, String> p = new LinkedMultiValueMap<>();
        p.add("account_id", "113");
        p.add("content_type", "json");
        p.add("thoroughfare", "alicard_auto");
        //p.add("type", "1");
        p.add("out_trade_no", sysOrderId);

        p.add("amount", amount);
        p.add("callback_url", "http://47.99.180.135");
        p.add("success_url", "success_url");
        p.add("error_url", "error_url");
        p.add("sign", encrypt(Double.valueOf(amount), sysOrderId));
        p.add("sign_type", "1");

        //提交请求
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(p, headers);

        String result = restTemplate.postForObject("http://zf.13wmb.cn/gateway/index/checkpoint.do", entity, String.class);

        return result;
    }


    //加密方法
    public String encrypt(double money, String orderNumber) {

        //将传入的金额保留两位小数，然后拼接订单号，加密成MD5格式
        String data = getMd5((df.format(money) + orderNumber).getBytes());

        String cipher = "";
        int[] key = new int[256];
        int[] box = new int[256];
        int pwd_length = KEY.length();
        int data_length = data.length();

        for (int i = 0; i < 256; i++) {
            key[i] = Integer.parseInt(stringToAscii(String.valueOf(KEY.charAt(i % pwd_length))));
            box[i] = i;
        }
        int j = 0;

        for (int i = 0; i < 256; i++) {
            j = (j + box[i] + key[i]) % 256;
            int tmp = box[i];
            box[i] = box[j];
            box[j] = tmp;
        }

        int b = 0, a = 0;
        for (int i = 0; i < data_length; i++) {
            a = (a + 1) % 256;
            b = (b + box[a]) % 256;
            int tmp = box[a];
            box[a] = box[b];
            box[b] = tmp;

            int k = box[((box[a] + box[b]) % 256)];
            cipher += strTo16((asciiToString(String.valueOf(Integer.parseInt(stringToAscii(String.valueOf(data.charAt(i)))) ^ k))));
        }
        return getMd5(cipher.getBytes());
    }

    //String 转 Ascii
    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append((int) chars[i]).append(",");
            } else {
                sbu.append((int) chars[i]);
            }
        }
        return sbu.toString();
    }

    //Ascii 转 String
    public static String asciiToString(String value) {
        String[] asciiArr = value.split(",");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < asciiArr.length; i++) {
            builder.append((char) Integer.parseInt(asciiArr[i]));
        }
        return builder.toString();
    }

    //String 转 16进制
    public static String strTo16(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            if (s4.length() == 1) {
                str = "0";
            }
            str = str + s4;
        }
        return str;
    }

    //MD5加密
    public static String getMd5(byte[] buffer) {
        String s = null;
        MessageDigest md = null;
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        md.update(buffer);
        byte[] datas = md.digest(); //16个字节的长整数
        char[] str = new char[2 * 16];
        int k = 0;
        for (int i = 0; i < 16; i++) {
            byte b = datas[i];
            str[k++] = hexChars[b >>> 4 & 0xf];//高4位
            str[k++] = hexChars[b & 0xf];//低4位
        }
        s = new String(str);
        return s;
    }


}
