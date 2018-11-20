package com.ylli.api.base.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Created by RexQian on 2017/6/15.
 */
@Component
public class MergeUtil {
    @Autowired
    private MappingJackson2HttpMessageConverter jsonConvert;

    Map merge(Map map, Object data) {
        if (data == null) {
            return map;
        }
        if (data instanceof Map) {
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) data).entrySet()) {
                map = merge(map, entry.getKey(), entry.getValue());
            }
            return map;
        }

        Map dataMap = jsonConvert.getObjectMapper().convertValue(data, Map.class);
        if (map == null) {
            return dataMap;
        }
        if (dataMap != null) {
            map.putAll(dataMap);
        }
        return map;
    }

    Map merge(Map map, Object... objects) {
        if (objects == null) {
            return map;
        }
        for (Object object : objects) {
            map = merge(map, object);
        }
        return map;
    }

    Map merge(Map map, String name, Object data) {
        if (data == null) {
            return map;
        }
        Object dataMap;
        ObjectMapper mapper = jsonConvert.getObjectMapper();
        if (data instanceof List || data.getClass().isArray()) {
            dataMap = mapper.convertValue(data, List.class);
        } else if (ClassUtils.isPrimitiveOrWrapper(data.getClass()) || data instanceof String) {
            dataMap = data;
        } else {
            dataMap = mapper.convertValue(data, Map.class);
        }
        if (dataMap != null) {
            PropertyNamingStrategy.PropertyNamingStrategyBase base =
                    (PropertyNamingStrategy.PropertyNamingStrategyBase) mapper.getPropertyNamingStrategy();
            name = base.translate(name);
            map.put(name, dataMap);
        }
        return map;
    }

    public Builder builder() {
        return new Builder(this);
    }

    public static class Builder {
        private MergeUtil mergeUtil;
        private Map map = new LinkedHashMap();

        private Builder(MergeUtil mergeUtil) {
            this.mergeUtil = mergeUtil;
        }

        public Builder merge(Object data) {
            map = mergeUtil.merge(map, data);
            return this;
        }

        public Builder merge(Object... objects) {
            map = mergeUtil.merge(map, objects);
            return this;
        }

        public Builder merge(String name, Object data) {
            map = mergeUtil.merge(map, name, data);
            return this;
        }

        public interface DataFunc {
            Object data();
        }

        public Builder merge(String name, DataFunc func) {
            return merge(name, func.data());
        }

        public Builder merge(DataFunc func) {
            return merge(func.data());
        }

        public Map create() {
            return map;
        }
    }
}
