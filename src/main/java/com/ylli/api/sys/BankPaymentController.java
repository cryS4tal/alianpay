package com.ylli.api.sys;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.sys.service.BankPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank/payment")
@Auth(@Permission(Config.SysPermission.MANAGE_BANK_PAYMENT))
public class BankPaymentController {

    @Autowired
    BankPaymentService bankPaymentService;

    static class Payment {
        public Long id;
        public Boolean isOpen;
    }

    /**
     * 开启关闭系统代付通道
     */
    @PostMapping("/sys")
    public void paymentSwitch(@RequestBody Payment payment) {
        bankPaymentService.paymentSwitch(payment.id, payment.isOpen);
    }

    @GetMapping("/sys")
    public Object bankPays(@AwesomeParam(defaultValue = "0") int offset,
                           @AwesomeParam(defaultValue = "20") int limit) {
        return bankPaymentService.bankPays(offset, limit);
    }
}
