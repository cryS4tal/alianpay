package com.ylli.api.pay;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.AwesomeDateTime;
import com.ylli.api.pay.service.BankPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bankpay")
@Auth
public class BankOrderController {

    @Autowired
    BankPayService bankPayService;

    @Autowired
    PermissionService permissionService;


    /**
     * @param mchId      商户号
     * @param status     订单状态0-new,1-ing,3-finish,4-fail
     * @param mchOrderId
     * @param sysOrderId
     * @param accName    收款姓名
     * @param payType    代付类型：1-对私，2-对公
     * @param tradeTime
     * @param startTime
     * @param endTime
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping
    public Object getOrders(@AwesomeParam(required = false) Long mchId,
                            @AwesomeParam(required = false) Integer status,
                            @AwesomeParam(required = false) String mchOrderId,
                            @AwesomeParam(required = false) String sysOrderId,
                            @AwesomeParam(required = false) String accName,
                            @AwesomeParam(required = false) Integer payType,
                            @AwesomeParam(required = false) AwesomeDateTime tradeTime,
                            @AwesomeParam(required = false) AwesomeDateTime startTime,
                            @AwesomeParam(required = false) AwesomeDateTime endTime,
                            @AwesomeParam(defaultValue = "0") int offset,
                            @AwesomeParam(defaultValue = "20") int limit) {
        if (mchId == null && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_BILL)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return bankPayService.getOrders(mchId, status, mchOrderId, sysOrderId, accName, payType,
                tradeTime == null ? null : tradeTime.getDate(),
                startTime == null ? null : startTime.getDate(),
                endTime == null ? null : endTime.getDate(), offset, limit);
    }
}
