package com.ylli.api.base.util;

import com.ylli.api.base.Config;
import com.ylli.api.base.exception.AwesomeException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ylli on 2017/5/26.
 */
public class ServiceUtil {

    /**
     * obj包含参数不可为null
     *
     * @param object javaBean
     */
    public static void checkNotEmpty(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field entityField : fields) {
            Field field = null;
            try {
                field = object.getClass().getDeclaredField(entityField.getName());
            } catch (NoSuchFieldException exception) {
                exception.printStackTrace();
            }
            try {
                if (null == field.get(object)) {
                    throw new AwesomeException(Config.ERROR_PARAM_NOT_NULL.format(field.getName()));
                }
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * obj包含参数不可为null，忽略 id, createTime, modifyTime
     *
     * @param object javaBean
     */
    public static void checkNotEmptyIgnore(Object object, Boolean ignore, String... args) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field entityField : fields) {
            Field field = null;
            try {
                IGNORE_LIST.addAll(Arrays.asList(args));
                if (ignore && IGNORE_LIST.contains(entityField.getName())) {
                    continue;
                }
                field = object.getClass().getDeclaredField(entityField.getName());
            } catch (NoSuchFieldException exception) {
                exception.printStackTrace();
            }
            try {
                if (null == field.get(object)) {
                    throw new AwesomeException(Config.ERROR_PARAM_NOT_NULL.format(field.getName()));
                }
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static List<String> IGNORE_LIST = new ArrayList<String>() {
        {
            add("id");
            add("createTime");
            add("modifyTime");
        }
    };

    /**
     * 更新新参数至原有对象
     *
     * @param originObj  源对象
     * @param currentObj 现对象
     * @throws IllegalAccessException 非法参数
     */
    public static Object update(Object originObj, Object currentObj) {
        Field[] orgFields = originObj.getClass().getDeclaredFields();
        Field[] curFields = currentObj.getClass().getDeclaredFields();
        for (Field orgField : orgFields) {
            for (Field curField : curFields) {
                try {
                    if (orgField.equals(curField) && curField.get(currentObj) != null) {
                        orgField.set(originObj, curField.get(currentObj));
                    }
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return originObj;
    }
}
