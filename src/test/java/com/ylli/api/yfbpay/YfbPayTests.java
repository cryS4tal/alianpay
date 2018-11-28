package com.ylli.api.yfbpay;

import com.ylli.api.yfbpay.service.YfbClient;
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
//@Ignore
public class YfbPayTests {

    @Autowired
    YfbClient client;

    @Test
    public void createOrder() throws Exception {
        client.order("1004", "1.00", "20181128test15", "http://127.0.0.1:8080", "", "", "");
    }

}
