package com.ylli.api.pingan.model;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * *********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.core.utils
 * @Author: yangdf
 * @Date 2018/5/4 13:17
 * @Description: gson 工具类
 * @ModifyDetail
 * @ModifyDate
 */
public class GsonUtil {

    /**
     * @Author yangdf
     * @Description object 转成json字符串
     * @Params [object]
     * @ReturnType java.lang.String
     * @Date 2018/5/4 13:18
     * @ModifyDetail
     * @ModifyDate
     */
    public static String objToJson(Object object) {
        return new GsonBuilder().create().toJson(object);
    }

    /**
     * @Author yangdf
     * @Description json 转 bean
     * @Params [json, clazz]
     * @ReturnType T
     * @Date 2018/5/4 13:21
     * @ModifyDetail
     * @ModifyDate
     */
    public static <T> T jsonToBean(String json, Class<T> clazz) {
        return new GsonBuilder().create().fromJson(json, clazz);
    }

    /**
     * @Author yangdf
     * @Description json转Map(String, Object)
     * @Params [json]
     * @ReturnType java.util.Map<java.lang.String,java.lang.Object>
     * @Date 2018/6/12 19:40
     * @ModifyDetail
     * @ModifyDate
     */
    public static Map<String, Object> jsonToMapObj(String json) {
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return new GsonBuilder().enableComplexMapKeySerialization().create().fromJson(json, type);
    }


    /**
     * @Author yangdf
     * @Description json转Map(String, String)
     * @Params [json]
     * @ReturnType java.util.Map<java.lang.String,java.lang.String>
     * @Date 2018/6/12 19:40
     * @ModifyDetail
     * @ModifyDate
     */
    public static Map<String, String> jsonToMapStr(String json) {
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return new GsonBuilder().enableComplexMapKeySerialization().create().fromJson(json, type);
    }
}
