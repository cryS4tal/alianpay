package com.ylli.api.third.pay;

import com.ylli.api.third.pay.service.KyPayService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 此类并非接入原生ali支付. 对接产品为支付供应商 kypay 提供的支付宝通道
 */
@RestController
@RequestMapping("/pay/ky")
public class KyPayController {

    @Autowired
    KyPayService kyPayService;

    /**
     * 创建订单
     *
     * @return
     */
    @PostMapping("/order")
    public Object createAliPayOrder() {
        return kyPayService.createAliPayOrder();
    }

    /**
     * 查询订单
     *
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
        kyPayService.payNotify(request, response);
    }
}
