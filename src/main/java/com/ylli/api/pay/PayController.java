package com.ylli.api.pay;

import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加入版本号..
 * 兼容多通道
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Value("${order.enable}")
    public Boolean enable;

    @Autowired
    PayService payService;

    @PostMapping("/order")
    public Object createOrder(@RequestBody BaseOrder baseOrder) throws Exception {

        if (!enable) {
            return new Response("999", "系统维护，请稍后再试.");
        }
        return payService.createOrder(baseOrder);
    }

    @PostMapping("/order/query")
    public Object orderQuery(@RequestBody OrderQueryReq orderQuery) throws Exception {

        return payService.orderQuery(orderQuery);
    }

}
