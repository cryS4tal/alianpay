package com.ylli.api.pay;

import com.ylli.api.auth.service.AccountService;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.ResponseEnum;
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

    @Value("${bank.pay.enable}")
    public Boolean enable;

    @Autowired
    BankPayService bankPayService;

    @Autowired
    AccountService accountService;


    @PostMapping("/order")
    public Object createOrder(@RequestBody BankPayOrder bankPayOrder) throws Exception {

        if (!enable) {
            return ResponseEnum.A999(null, null);
        }
        if (!accountService.isActive(bankPayOrder.mchId)) {
            return ResponseEnum.A100(null, null);
        }
        return bankPayService.createOrder(bankPayOrder);
    }

    @PostMapping("/order/query")
    public Object orderQuery(@RequestBody OrderQueryReq orderQuery) throws Exception {
        if (!accountService.isActive(orderQuery.mchId)) {
            return ResponseEnum.A100(null, null);
        }
        return bankPayService.orderQuery(orderQuery);
    }
}
