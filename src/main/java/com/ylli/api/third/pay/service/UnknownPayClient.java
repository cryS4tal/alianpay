package com.ylli.api.third.pay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UnknownPayClient {

    public String uid = "s1000958";

    public String token = "747b6c0c4861bfd6ddf48dd4c21c8625";

    @Autowired
    RestTemplate restTemplate;




}
