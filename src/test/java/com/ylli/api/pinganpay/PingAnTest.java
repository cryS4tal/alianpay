package com.ylli.api.pinganpay;

import com.ylli.api.third.pay.service.PingAnService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Ignore
public class PingAnTest {

    @Autowired
    PingAnService service;

    @Test
    public void createOrder() {
        service.createPingAnOrder();
    }
}
