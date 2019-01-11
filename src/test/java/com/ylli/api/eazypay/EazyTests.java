package com.ylli.api.eazypay;

import com.ylli.api.third.pay.service.EazyClient;
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
public class EazyTests {

    @Autowired
    EazyClient eazyClient;

    @Test
    public void order() {
        eazyClient.createOrder("alipay","20190111A00001","1.00");
    }
}
