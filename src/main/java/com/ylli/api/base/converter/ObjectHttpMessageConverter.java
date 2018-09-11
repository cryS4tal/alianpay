package com.ylli.api.base.converter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

/**
 * <p>Converts HTTP requests with bodies that are application/x-www-form-urlencoded or multipart/form-data to an Object
 * annotated with {@link org.springframework.web.bind.annotation.RequestBody} in the the handler method.
 *
 * @author Jesse Swidler
 */
public class ObjectHttpMessageConverter implements HttpMessageConverter<Object> {

    private final FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Charset charset = StandardCharsets.UTF_8;

    public ObjectHttpMessageConverter() {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public void setPropertyNamingStrategy(PropertyNamingStrategy strategy) {
        objectMapper.setPropertyNamingStrategy(strategy);
    }

    @Override
    public boolean canRead(Class clazz, MediaType mediaType) {
        return objectMapper.canSerialize(clazz) && formHttpMessageConverter.canRead(MultiValueMap.class, mediaType);
    }

    @Override
    public boolean canWrite(Class clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return formHttpMessageConverter.getSupportedMediaTypes();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object read(Class clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        MediaType contentType = inputMessage.getHeaders().getContentType();
        Charset charset = (contentType.getCharset() != null ? contentType.getCharset() : this.charset);
        String body = StreamUtils.copyToString(inputMessage.getBody(), charset);

        String[] pairs = StringUtils.tokenizeToStringArray(body, "&");
        Map<String, Object> objectMap = new HashMap<>();
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            String name;
            String value;
            if (idx == -1) {
                name = URLDecoder.decode(pair, charset.name());
                value = null;
            } else {
                name = URLDecoder.decode(pair.substring(0, idx), charset.name());
                value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
            }
            Object existValue = objectMap.get(name);
            if (existValue == null) {
                objectMap.put(name, value);
            } else if (existValue instanceof List) {
                ((List<String>) existValue).add(value);
            } else {
                List<String> list = new ArrayList<>();
                list.add((String) existValue);
                list.add(value);
                objectMap.put(name, list);
            }
        }

        String json = new Gson().toJson(objectMap);
        return objectMapper.readValue(json, clazz);
    }

    @Override
    public void write(Object obj, MediaType contentType, HttpOutputMessage outputMessage)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("");
    }
}
