package com.ylli.api.pay.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 签名公共类
 * <p>
 * md5
 */
@SuppressWarnings("all")
public class SignUtil {


    /**
     * 签名处理
     *
     * @param data 待签名数据
     * @param key  私钥
     * @return 签名
     */
    public static String generateSignature(final Map<String, String> data, String key) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if (k.equals("sign")) {
                continue;
            }
            if (data.get(k).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(data.get(k).trim()).append("&");
        }
        sb.append("key=").append(key);
        System.out.println(MD5(sb.toString()).toUpperCase());
        return MD5(sb.toString()).toUpperCase();
    }

    /**
     * 生成 MD5
     *
     * @param data 待处理数据
     * @return MD5结果
     */
    public static String MD5(String data) throws Exception {
        java.security.MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 生成 MD5
     *
     * @param data        待处理数据
     * @param charsetName 编码
     * @return MD5结果
     */
    public static String MD5(String data, String charsetName) throws Exception {
        java.security.MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(data.getBytes(charsetName));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }


    /**
     * 判断签名是否正确
     *
     * @param xmlStr XML格式数据
     * @param key    API密钥
     * @return 签名是否正确
     * @throws Exception
     */
    public static boolean isSignatureValid(String xmlStr, String key) throws Exception {
        Map<String, String> data = xmlToMap(xmlStr);
        if (!data.containsKey("sign")) {
            return false;
        }
        String sign = data.get("sign");
        return generateSignature(data, key).equals(sign);
    }

    /**
     * XML格式字符串转换为Map
     *
     * @param strXML XML字符串
     * @return XML数据转换后的Map
     * @throws Exception
     */
    public static Map<String, String> xmlToMap(String strXML) throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
        org.w3c.dom.Document doc = documentBuilder.parse(stream);
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getDocumentElement().getChildNodes();
        for (int idx = 0; idx < nodeList.getLength(); ++idx) {
            Node node = nodeList.item(idx);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                data.put(element.getNodeName(), element.getTextContent());
            }
        }
        try {
            stream.close();
        } catch (Exception ex) {

        }
        return data;
    }

    public static Map objectToMap(Object object) {
        Map<String, String> map = new LinkedHashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field entityField : fields) {
            Field field = null;
            try {
                field = object.getClass().getDeclaredField(entityField.getName());
                map.put(field.getName(), field.get(object) == null ? "" : field.get(object).toString());
            } catch (NoSuchFieldException exception) {
                exception.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
