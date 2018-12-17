package com.ylli.api.pingan.model;

import com.alibaba.fastjson.JSONObject;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.util.StringUtils;

public class XmlRequestUtil {

    public static String createXmlRequest(JSONObject paramMap) {
        Element result = DocumentHelper.createElement("Result");
        for (String key : paramMap.keySet()) {
            Element keyEl = DocumentHelper.createElement(upperCase(key));
            if (!StringUtils.isEmpty(paramMap.get(key))) {
                keyEl.addText(paramMap.get(key).toString());
            }
            result.add(keyEl);
        }
        return result.asXML();
    }


    public static String upperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
