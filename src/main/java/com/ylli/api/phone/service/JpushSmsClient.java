package com.ylli.api.phone.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.phone.Config;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by RexQian on 2017/4/27.
 */
public class JpushSmsClient {

    private static Logger LOGGER = LoggerFactory.getLogger(JpushSmsClient.class);

    private String base64AuthString;

    private RestTemplate restTemplate;

    public JpushSmsClient(String appId, String appSecret) {
        base64AuthString = "Basic "
                + Base64.getEncoder().encodeToString((appId + ":" + appSecret).getBytes(StandardCharsets.UTF_8));
        initRestTemplate();
    }

    private void initRestTemplate() {
        restTemplate = new RestTemplate();

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            request.getHeaders().set("Authorization", base64AuthString);
            return execution.execute(request, body);
        };
        interceptors.add(interceptor);
        restTemplate.setInterceptors(interceptors);

        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMessageConverter.setObjectMapper(objectMapper);
        messageConverters.add(jsonMessageConverter);
        restTemplate.setMessageConverters(messageConverters);
    }

    public static class JpushError {
        public String code;
        public String message;
    }

    public static class JpushSendCodeReq {
        public String mobile;
        public String tempId;
    }

    public static class JpushSendCodeRsp {
        public String msgId;
        public JpushError error;
    }

    public String sendCode(String phone, String tempId) {

        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "https://api.sms.jpush.cn/v1/codes").toUriString();
        try {
            JpushSendCodeReq req = new JpushSendCodeReq();
            req.mobile = phone;
            req.tempId = tempId;
            JpushSendCodeRsp result = restTemplate.postForObject(requestUrl, req, JpushSendCodeRsp.class);
            if (result.error != null) {
                LOGGER.warn(new Gson().toJson(result));
                throw new AwesomeException(Config.ERROR_SEND_VERIFY_CODE);
            }
            return result.msgId;
        } catch (RestClientResponseException ex) {
            LOGGER.warn(new Gson().toJson(ex.getResponseBodyAsString()));
            throw new AwesomeException(Config.ERROR_SEND_VERIFY_CODE);
        }
    }

    public static class JpushVerifyCodeReq {
        public String code;
    }

    public static class JpushVerifyCodeRsp {
        public Boolean isValid;
        public JpushError error;
    }

    public boolean verifyCode(String messageId, String code) {
        Pattern codePattern = Pattern.compile("^[0-9]{6}");
        if (code == null || !codePattern.matcher(code).matches()) {
            return false;
        }
        String requestUrl = UriComponentsBuilder.fromHttpUrl(
                "https://api.sms.jpush.cn/v1/codes/{msg_id}/valid")
                .buildAndExpand(messageId)
                .toUriString();
        try {
            JpushVerifyCodeReq req = new JpushVerifyCodeReq();
            req.code = code;
            JpushVerifyCodeRsp result = restTemplate.postForObject(requestUrl, req, JpushVerifyCodeRsp.class);
            if (result.error != null) {
                LOGGER.warn(new Gson().toJson(result));
                throw new AwesomeException(Config.ERROR_CODE_FORMAT.format(jpushCode2String(result.error.code)));
            }
            return result.isValid;
        } catch (RestClientResponseException ex) {
            LOGGER.warn(new Gson().toJson(ex.getResponseBodyAsString()));
            throw new AwesomeException(Config.ERROR_CODE);
        }
    }


    private static Map<String, String> ERROR_MAP = new HashMap<>();

    static {
        ERROR_MAP.put("50009", "发送超频");
        ERROR_MAP.put("50010", "验证码无效");
        ERROR_MAP.put("50011", "验证码过期");
    }

    private static String jpushCode2String(String errorCode) {
        String msg = ERROR_MAP.get(errorCode);
        return Strings.isNullOrEmpty(msg) ? "未知错误" : msg;
    }
}
