package com.ylli.api.third.pay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EazyClient {

    @Autowired
    RestTemplate restTemplate;

    public String createOrder() {
        return null;
    }
}
