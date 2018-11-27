package com.ylli.api.xfpay;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.xfpay.model.CreditPay;
import com.ylli.api.xfpay.service.XfPayService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/xfpay")
public class XfPayController {

    @Autowired
    XfPayService xfPayService;

    @Autowired
    AuthSession authSession;

    static class Request {
        public Long userId;

        public Integer amount;
        public String accountNo;
        public String accountName;
        public String mobileNo; //not required
        public String bankNo;
        public Integer userType;
        /**
         * 1（借记卡）
         * 2（贷记卡）
         * 4（对公账户）
         * not required.
         * <p>
         * userType = 1; accountType = (1,2) 默认1
         * userType = 2; accountType 默认4
         */
        public Integer accountType;
        public String memo;     //not required
        //商户订单号 - 对应 subNo.
        public String orderNo;
    }


    @PostMapping("/wage")
    @Auth
    public Object wagesPay(@RequestBody Request request) throws Exception {
        if (request.userId == null || authSession.getAuthId() != request.userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return xfPayService.wagesPay(request.userId, request.amount, request.accountNo, request.accountName,
                request.mobileNo, request.bankNo, request.userType, request.accountType, request.memo, request.orderNo);
    }

    @PostMapping("/notify")
    @Auth
    public void payNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
        xfPayService.payNotify(request, response);
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
        return xfPayService.orderQuery(request.userId, request.orderNo);
    }

    /**
     * 提供给服务商的带单代发接口.
     * @return
     */
    @PostMapping("/wage/credit")
    public Object wagesPayNoAuth(@RequestBody CreditPay pay) throws Exception {
        return xfPayService.wagesPayNoAuth(pay);
    }
}
