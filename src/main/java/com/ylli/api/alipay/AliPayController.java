package com.ylli.api.alipay;

import com.ylli.api.alipay.service.AliPayService;
import com.ylli.api.base.annotation.Auth;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/ali")
@Auth
public class AliPayController {

    @Autowired
    AliPayService aliPayService;

    /**
     * 创建订单
     * @return
     */
    @PostMapping("/order")
    public Object createAliPayOrder() {
        return aliPayService.createAliPayOrder();
    }

    /**
     * 查询订单
     * @return
     */
    @GetMapping("/order")
    public Object queryAliPayOrder() {
        return null;
    }

    /**
     * 支付回调
     */
    @PostMapping("/order/notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        aliPayService.payNotify(request,response);
    }
}
