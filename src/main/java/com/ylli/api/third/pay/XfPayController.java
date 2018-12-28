package com.ylli.api.third.pay;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.third.pay.model.CreditPay;
import com.ylli.api.third.pay.service.XianFenService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/xf")
public class XfPayController {

    @Autowired
    XianFenService xianFenService;

    @Autowired
    AuthSession authSession;

    @PostMapping("/notify")
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        xianFenService.payNotify(request, response);
    }

    static class A {
        public Long userId;
        //平台订单号
        //开放给下游服务商需要变成 商户订单号
        public String orderNo;
    }

    @PostMapping("/query")
    @Auth
    public Object orderQuery(@RequestBody A request) throws Exception {
        if (request.userId == null || authSession.getAuthId() != request.userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return xianFenService.orderQuery(request.userId, request.orderNo);
    }
}
