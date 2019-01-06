package com.ylli.api.pay;

import com.ylli.api.auth.service.AccountService;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.OrderConfirm;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.ResponseEnum;
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

@RestController
@RequestMapping("/pay")
public class PayController {

    @Value("${order.enable}")
    public Boolean enable;

    @Autowired
    PayService payService;

    @Autowired
    AccountService accountService;

    /**
     * 加入了版本控制.
     * version = 1.1 (CNT支付)
     */
    @PostMapping("/order")
    public Object createOrder(@RequestBody BaseOrder baseOrder) throws Exception {

        if (!enable) {
            return ResponseEnum.A999(null, null);
        }
        if (!accountService.isActive(baseOrder.mchId)) {
            return ResponseEnum.A100(null, null);
        }
        return payService.createOrder(baseOrder);
    }

    @PostMapping("/order/query")
    public Object orderQuery(@RequestBody OrderQueryReq orderQuery) throws Exception {
        if (!accountService.isActive(orderQuery.mchId)) {
            return ResponseEnum.A100(null, null);
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

    /**
     * 用于 version = 1.1 (cnt支付) 手动确认支付状态。
     */
    @PostMapping("/confirm")
    public Object payConfirm(@RequestBody OrderConfirm confirm) throws Exception {
        if (!accountService.isActive(confirm.mchId)) {
            return ResponseEnum.A100(null, null);
        }
        return payService.payConfirm(confirm);
    }

    /**
     * 测试，手动给商户发起回调
     * @param mchOrderId
     * @return
     * @throws Exception
     */
    @GetMapping("/manual/notify")
    public Object manualNotify(@AwesomeParam String mchOrderId) throws Exception {
        return payService.manualNotify(mchOrderId);
    }

}
