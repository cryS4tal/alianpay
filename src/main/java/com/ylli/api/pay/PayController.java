package com.ylli.api.pay;

import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.ResponseEnum;
import com.ylli.api.pay.service.PayService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Value("${order.enable}")
    public Boolean enable;

    @Autowired
    PayService payService;

    @PostMapping("/order")
    public Object createOrder(@RequestBody BaseOrder baseOrder,
                              @RequestHeader("Content-Type") String contentType) throws Exception {
        if (!MediaType.APPLICATION_JSON_VALUE.equals(contentType) && !MediaType.APPLICATION_JSON_UTF8_VALUE.equals(contentType)) {
            return ResponseEnum.A003("Content-Type should be application/json", null);
        }
        if (!enable) {
            return ResponseEnum.A999(null, null);
        }
        return payService.createOrder(baseOrder);
    }

    @PostMapping("/order/query")
    public Object orderQuery(@RequestBody OrderQueryReq orderQuery,
                             @RequestHeader("Content-Type") String contentType) throws Exception {
        if (!MediaType.APPLICATION_JSON_VALUE.equals(contentType) && !MediaType.APPLICATION_JSON_UTF8_VALUE.equals(contentType)) {
            return ResponseEnum.A003("Content-Type should be application/json", null);
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
     * 手动给商户发起回调
     */
    @GetMapping("/manual/notify")
    public Object manualNotify(@AwesomeParam String mchOrderId) throws Exception {
        return payService.manualNotify(mchOrderId);
    }

}
