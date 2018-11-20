package com.ylli.api.alipay;

import com.google.gson.Gson;
import com.ylli.api.alipay.model.OrderQueryResponse;
import com.ylli.api.alipay.model.OrderResponse;
import com.ylli.api.alipay.service.AliPayClient;
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
//@Ignore
public class AlipayTests {

    @Autowired
    AliPayClient aliPayClient;

    @Test
    public void createOrder() {
        OrderResponse response = aliPayClient.createAliPayOrder();
        System.out.println(response);
    }

    @Test
    public void orderQuery(){
        OrderQueryResponse response = aliPayClient.aliPayOrderQuery();
        System.out.println(new Gson().toJson(response));
    }
}
