package com.ylli.api.wallet;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.wallet.model.CashReq;
import com.ylli.api.wallet.service.CashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cash")
@Auth
public class CashController {

    @Autowired
    CashService cashService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    @GetMapping("/list")
    public Object cashList(@AwesomeParam(required = false) Long mchId,
                           @AwesomeParam(required = false) String phone,
                           @AwesomeParam(defaultValue = "0") int offset,
                           @AwesomeParam(defaultValue = "20") int limit) {
        if (mchId == null && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_CASH)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return cashService.cashList(mchId, phone, offset, limit);
    }

    @PostMapping
    public void cash(@RequestBody CashReq req) {
        ServiceUtil.checkNotEmptyIgnore(req, true, "identityCard", "reservedPhone");
        if (authSession.getAuthId() != req.mchId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        if (req.money > 5 * 10000 * 100 || req.money < 10 * 100) {
            throw new AwesomeException(Config.ERROR_CHARGE_MONEY);
        }
        cashService.cash(req);
    }

    /**
     * 临时方法。满足手动提现成功.
     */
    static class Suc {
        public Long cashLogId;
        public Boolean success;
    }

    @PostMapping("/success")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_CASH))
    public void success(@RequestBody Suc suc) {
        cashService.success(suc.cashLogId, suc.success);
    }

}
