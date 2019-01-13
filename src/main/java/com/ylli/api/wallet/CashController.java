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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cash")
@Auth
public class CashController {

    @Value("${cash.min}")
    public Integer min;

    @Value("${cash.max}")
    public Integer max;

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

    /**
     * 商户发起提现请求
     *
     * @param req
     */
    @PostMapping
    public void cash(@RequestBody CashReq req) throws Exception {
        ServiceUtil.checkNotEmptyIgnore(req, true, "identityCard", "reservedPhone");
        if (authSession.getAuthId() != req.mchId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        if (req.money > max || req.money < min) {
            throw new AwesomeException(Config.ERROR_CHARGE_MONEY);
        }
        cashService.cash(req);
    }

    /**
     * 手工代付。.
     * success  = true. 手工代付成功
     * success = false. 拒绝
     */
    static class Suc {
        public Long cashLogId;
        public Boolean success;
    }

    @PostMapping("/manual")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_CASH))
    public void manualCash(@RequestBody Suc suc) {
        cashService.manualCash(suc.cashLogId, suc.success);
    }

    /**
     * 系统代付
     */
    static class Sys {
        //系统代付通道
        public Long bankPayId;
        public Long cashLogId;
    }

    @PostMapping("/sys")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_CASH))
    public void sysCash(@RequestBody Sys sys) throws Exception {
        //cashService.sysCash(sys.bankPayId, sys.cashLogId);
    }
}
