package com.ylli.api.yfbpay;

import com.ylli.api.third.pay.service.YfbClient;
import org.junit.Ignore;
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
public class YfbPayTests {

    @Autowired
    YfbClient client;

    /*@Test
    public void createOrder() throws Exception {
        //client.order("992", "1.00", "20181128test20", "http://116.62.209.131:8088/pay/yfb/notify", "", "", "");

        //client.sendNotify();
        String string = client.orderQuery("20181128test20");
        System.out.println(string);
    }*/

}
