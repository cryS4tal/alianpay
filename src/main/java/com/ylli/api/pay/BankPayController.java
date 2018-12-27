package com.ylli.api.pay;

import com.ylli.api.auth.service.AccountService;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.BankPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bankpay")
public class BankPayController {

    @Value("${order.enable}")
    public Boolean enable;

    @Autowired
    BankPayService bankPayService;

    @Autowired
    AccountService accountService;


    @PostMapping("/order")
    public Object createOrder(@RequestBody BankPayOrder bankPayOrder) throws Exception {

        if (!enable) {
            return new Response("A999", "系统维护，请稍后再试.");
        }
        if (!accountService.isActive(bankPayOrder.mchId)) {
            return new Response("A100", "商户被冻结，请联系管理员");
        }
        return bankPayService.createOrder(bankPayOrder);
    }

    @PostMapping("/order/query")
    public Object orderQuery(@RequestBody OrderQueryReq orderQuery) throws Exception {
        if (!accountService.isActive(orderQuery.mchId)) {
            return new Response("A100", "商户被冻结，请联系管理员");
        }
        return bankPayService.orderQuery(orderQuery);
    }

}
