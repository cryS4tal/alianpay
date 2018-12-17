package com.ylli.api.pingan.model;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class YQUtil {
    public static final String CHARSET = "UTF-8";
    private static final String fmtTime = "yyyyMMddHHmmss";

    /**
     * 组装报文
     * 这里append比较多，是为了展现报文头的各个字段；实际使用中请按需减少
     *
     * @param yqdm    20位银企代码
     * @param bsnCode 交易代码
     * @param xmlBody xml主体报文
     * @return
     */
    public static String asemblyPackets(String yqdm, String bsnCode, String xmlBody) {
        Date now = Calendar.getInstance().getTime();
        StringBuilder buf = new StringBuilder();
        buf.append("A00101");
        //编码
        String encoding = "01";
        if (CHARSET.equalsIgnoreCase("GBK")) {
            encoding = "01";
        } else if (CHARSET.equalsIgnoreCase("utf-8") || CHARSET.equalsIgnoreCase("utf8")) {
            encoding = "02";
        }
        buf.append(encoding);//编码
        buf.append("01");//通讯协议为TCP/IP
        buf.append(String.format("%20s", yqdm));//银企代码
        try {
            buf.append(String.format("%010d", xmlBody.getBytes(CHARSET).length));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        buf.append(String.format("%-6s", bsnCode));//交易码-左对齐
        buf.append("12345");//操作员代码-用户可自定义
        buf.append("01");//服务类型 01请求

        String fmtNow = new SimpleDateFormat(fmtTime).format(now);
        buf.append(fmtNow); //请求日期时间

        String requestLogNo = "YQTEST" + fmtNow;
        buf.append(requestLogNo);//请求方系统流水号

        buf.append(String.format("%6s", "")); //返回码
        buf.append(String.format("%100s", ""));

        buf.append(0); //后续包标志
        buf.append(String.format("%03d", 0));//请求次数
        buf.append("0");//签名标识 0不签
        buf.append("1");//签名数据包格式
        buf.append(String.format("%12s", "")); //签名算法
        buf.append(String.format("%010d", 0)); //签名数据长度
        buf.append(0);//附件数目
        buf.append(xmlBody);//报文体

        return buf.toString();
    }
}
