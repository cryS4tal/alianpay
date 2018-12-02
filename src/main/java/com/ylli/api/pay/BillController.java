package com.ylli.api.pay;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.AwesomeDateTime;
import com.ylli.api.pay.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bill")
@Auth
public class BillController {

    @Autowired
    BillService billService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    /**
     * 金额
     * 交易时间
     * 状态
     * 渠道：支付宝
     * 系统订单号
     * 商户订单号
     */

    @GetMapping
    public Object getBills(@AwesomeParam(required = false) Long userId,
                           @AwesomeParam(required = false) Integer status,
                           @AwesomeParam(required = false) String mchOrderId,
                           @AwesomeParam(required = false) String sysOrderId,
                           @AwesomeParam(required = false) String payType,
                           @AwesomeParam(required = false) String tradeType,
                           @AwesomeParam(required = false) AwesomeDateTime tradeTime,
                           @AwesomeParam(required = false) AwesomeDateTime startTime,
                           @AwesomeParam(required = false) AwesomeDateTime endTime,
                           @AwesomeParam(defaultValue = "0") int offset,
                           @AwesomeParam(defaultValue = "20") int limit) {
        if (userId == null && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_BILL)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return billService.getBills(userId, status, mchOrderId, sysOrderId, payType, tradeType,
                tradeTime == null ? null : tradeTime.getDate(),
                startTime == null ? null : startTime.getDate(),
                endTime == null ? null : endTime.getDate(), offset, limit);
    }

    @GetMapping("/today")
    public Object getTodayDetail(@AwesomeParam(required = false) Long userId) {
        do {
            if (userId != null && authSession.getAuthId() == userId) {
                break;
            }
            if (permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_BILL)) {
                break;
            }
            permissionService.permissionDeny();
        } while (false);
        return billService.getTodayDetail(userId);
    }

}
