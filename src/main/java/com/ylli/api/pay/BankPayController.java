package com.ylli.api.pay;

import com.ylli.api.auth.service.AccountService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.pay.model.BankPayOrder;
import com.ylli.api.pay.model.OrderQueryReq;
import com.ylli.api.pay.model.ResponseEnum;
import com.ylli.api.pay.service.BankPayService;
import com.ylli.api.third.pay.service.xianfen.XianFenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Autowired
    XianFenService xianFenService;

    @Autowired
    AuthSession authSession;


    @PostMapping("/order")
    public Object createOrder(@RequestBody BankPayOrder bankPayOrder) throws Exception {

        if (!enable) {
            return ResponseEnum.A999(null, null);
        }
        if (bankPayOrder.mchId == null) {
            return ResponseEnum.A003("mch_id not empty, or please check Content-Type is application/json ?", null);
        }
        if (!accountService.isActive(bankPayOrder.mchId)) {
            return ResponseEnum.A100(null, null);
        }
        return bankPayService.createOrder(bankPayOrder);
    }

    @PostMapping("/order/query")
    public Object orderQuery(@RequestBody OrderQueryReq orderQuery) throws Exception {
        if (orderQuery.mchId == null) {
            return ResponseEnum.A003("mch_id not empty, or please check Content-Type is application/json ?", null);
        }
        if (!accountService.isActive(orderQuery.mchId)) {
            return ResponseEnum.A100(null, null);
        }
        return bankPayService.orderQuery(orderQuery);
    }

    /**
     * 目前系统走先锋支付.临时失败方法 todo delete
     *
     * @param sysOrderId
     * @return
     */
    @GetMapping("/fail")
    @Auth
    public Object fail(@AwesomeParam String sysOrderId) throws Exception {
        if (authSession.getAuthId() != 1002) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return xianFenService.fail(sysOrderId);
    }
}
