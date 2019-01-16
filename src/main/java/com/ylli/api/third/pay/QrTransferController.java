package com.ylli.api.third.pay;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个码转账.
 * 原生支付系统，不依赖任何第三方，需手动确认
 */
@RestController
@RequestMapping("/pay/qr")
public class QrTransferController {

    /**
     * 登录，登出（登出将不在派单.）
     * 设置个码账号。
     * 手动确认订单。
     * todo 回滚订单
     */


    @PostMapping("/code")
    public Object uploadQrCode() {
        return null;
    }

}
