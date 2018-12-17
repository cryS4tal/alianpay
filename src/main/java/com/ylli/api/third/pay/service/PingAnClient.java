package com.ylli.api.third.pay.service;

import java.util.Arrays;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PingAnClient {

    private static Logger LOGGER = LoggerFactory.getLogger(PingAnClient.class);

    private RestTemplate restTemplate;

    @PostConstruct
    private void initRestTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(6000);
        httpRequestFactory.setConnectTimeout(6000);
        httpRequestFactory.setReadTimeout(6000);

        restTemplate = new RestTemplate(httpRequestFactory);
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }


    public String orderTest(String xmlStr, String url) {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("text/xml; charset=UTF-8");
        headers.setContentType(type);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
        //headers.setAccept(Arrays.asList(MediaType.TEXT_XML));
        //http.setRequestProperty("Content-Length", String.valueOf(packets.length));
        //http.setRequestProperty("Connection", "close");
        //http.setRequestProperty("User-Agent", "sdb client");

        ResponseEntity<String> response = null;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<Object>(xmlStr, headers), String.class);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
        String body = response.getBody();
        HttpStatus status = response.getStatusCode();
        if (status == HttpStatus.OK) {
            System.out.println(status);
        }
        return response.getBody();
    }
}
