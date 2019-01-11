package com.ylli.api.pay.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;


/**
 * Created by: shufeng
 * Created on: 2018/12/19
 * Function:
 */

public class Utils {

    DecimalFormat df = new DecimalFormat("#0.00");

    //加密方法
    public String encrypt(double money, String orderNumber, String key_id) {

        //将传入的金额保留两位小数，然后拼接订单号，加密成MD5格式
        String data = getMd5((df.format(money) + orderNumber).getBytes());

        String cipher = "";
        int[] key = new int[256];
        int[] box = new int[256];
        int pwd_length = key_id.length();
        int data_length = data.length();

        for (int i = 0; i < 256; i++) {
            key[i] = Integer.parseInt(stringToAscii(String.valueOf(key_id.charAt(i % pwd_length))));
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


    public static void main(String[] args) {
        System.out.println(new Utils().encrypt(1.00, "test93732", "6AF0E51FD8B934"));
    }

}
