package com.ylli.api.pay;

import com.google.common.base.Strings;
import com.ylli.api.auth.service.AccountService;
import com.ylli.api.pay.enums.Version;
import com.ylli.api.pay.model.BaseOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.Response;
import com.ylli.api.pay.service.PayService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
            return new Response("A999", "系统维护，请稍后再试.");
        }
        if (!accountService.isActive(baseOrder.mchId)) {
            return new Response("A100", "商户被冻结，请联系管理员");
        }
        if (!Strings.isNullOrEmpty(baseOrder.version) && baseOrder.version.equals(Version.CNT.getVersion())) {
            return payService.createOrderCNT(baseOrder);
        } else {
            return payService.createOrderDefault(baseOrder);
        }
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
}
