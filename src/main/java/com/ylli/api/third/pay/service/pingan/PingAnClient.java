package com.ylli.api.third.pay.service.pingan;

import java.util.Arrays;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PingAnClient {

    private static Logger LOGGER = LoggerFactory.getLogger(PingAnClient.class);

    private RestTemplate restTemplate;

    @PostConstruct
    private void initRestTemplate() {
        restTemplate = new RestTemplate();
        setTimeout(restTemplate);
    }

    private void setTimeout(RestTemplate restTemplate) {
        if (restTemplate.getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(6 * 1000);
            ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(6 * 1000);
        } else if (restTemplate.getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(6 * 1000);
            ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(6 * 1000);
        }
    }


    public String post(String xmlStr, String url) {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("text/xml; charset=UTF-8");
        headers.setContentType(type);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<Object>(xmlStr, headers), String.class);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        if (response == null) {
            return null;
        }
        return response.getBody();
    }
}
