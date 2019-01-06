package com.ylli.api.pay.util;

import org.apache.commons.codec.binary.Base64;

@SuppressWarnings("all")
public class MyBase64 {
    // 将 BASE64 编码的字符串 s 进行解码
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
}
