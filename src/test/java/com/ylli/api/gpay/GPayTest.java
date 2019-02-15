package com.ylli.api.gpay;

import com.ylli.api.third.pay.service.gpay.GPayClient;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class GPayTest {

    @Autowired
    GPayClient gPayClient;

    @Test
    public void test() {
        gPayClient.createOrder(100,"alipay1");
        //messageService.addNotifyJobs(0L, "testUrl", "testParams");
    }
}
