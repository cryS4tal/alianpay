package com.ylli.api.xfpay;

import com.ylli.api.xfpay.service.XfClient;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by ylli on 2017/4/18.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class XfpayTests {

    @Autowired
    XfClient client;

    @Test
    public void createOrder() {
        /*String s = client.agencyPayment();
        System.out.println(s);*/
    }

    @Test
    public void orderQuery() {
        String s = client.orderQuery("111");
        System.out.println(s);
    }
}
