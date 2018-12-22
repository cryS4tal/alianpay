package com.ylli.api.pay;

import com.ylli.api.auth.service.AccountService;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.PayService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 加入版本号..
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Value("${order.enable}")
    public Boolean enable;

    @Autowired
    PayService payService;

    @Autowired
    AccountService accountService;

    @PostMapping("/order")
    public Object createOrder(@RequestBody BaseOrder baseOrder) throws Exception {

        if (!enable) {
            return new Response("999", "系统维护，请稍后再试.");
        }
        if (!accountService.isActive(baseOrder.mchId)) {
            return new Response("A100", "商户被冻结，请联系管理员");
        }
        return payService.createOrder(baseOrder);
    }

    @PostMapping("/order/query")
    public Object orderQuery(@RequestBody OrderQueryReq orderQuery) throws Exception {
        if (!accountService.isActive(orderQuery.mchId)) {
            return new Response("A100", "商户被冻结，请联系管理员");
        }
        return payService.orderQuery(orderQuery);
    }


    /**
     * 用于模拟商户系统支付回调
     */
    @PostMapping("/notify/test")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        payService.paynotify(request, response);
    }

    /*@GetMapping("/test/redis")
    public Object testRedis() {
        return payService.testRedis();
    }*/

    /*@GetMapping("/test/123")
    public Object testu(@AwesomeParam Integer count) throws Exception {
        return payService.testu(count);
    }*/

}
