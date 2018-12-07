package com.ylli.api.phone.service;

import com.google.common.base.Strings;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.phone.Config;
import com.ylli.api.phone.model.SiteVerify;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LuosimaoClient {

    private static Logger LOGGER = LoggerFactory.getLogger(LuosimaoClient.class);

    private RestTemplate restTemplate;

    //@Value("${sms.luosimao.api_key}")
    private String apiKey;

    @PostConstruct
    private void initRestTemplate() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public SiteVerify verify(String response) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        String str = "api_key=" + apiKey + "&response=" + response;
        ResponseEntity<SiteVerify> responseEntity;
        try {
            responseEntity = restTemplate.exchange("http://captcha.luosimao.com/api/site_verify",
                    HttpMethod.POST, new HttpEntity<Object>(str, headers), SiteVerify.class);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            throw new AwesomeException(Config.ERROR_SERVER);
        }
        SiteVerify siteVerify = responseEntity.getBody();
        if ("failed".equals(siteVerify.res)) {
            throw new AwesomeException(Config.ERROR_VERIFY.format(errorCodeString(siteVerify.error)));
        }
        return responseEntity.getBody();
    }

    private static Map<String, String> ERROR_MAP = new HashMap<>();

    static {
        ERROR_MAP.put("-10", "API KEY 为空");
        ERROR_MAP.put("-40", "API_KEY使用错误");
    }

    private static String errorCodeString(String errorCode) {
        String msg = ERROR_MAP.get(errorCode);
        return Strings.isNullOrEmpty(msg) ? "人机校验失败" : msg;
    }
}
