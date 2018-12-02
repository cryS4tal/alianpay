package com.ylli.api.wallet;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Auth
@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    WalletService walletService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    AuthSession authSession;

    static class Incr {
        public Long userId;
        public Integer money;
    }

    /**
     * todo 钱包加入日志表
     */

    @PostMapping
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_WALLET))
    public Object incr(@RequestBody Incr incr) {
        return walletService.incr(incr.userId, incr.money);
    }

    @GetMapping
    public Object getWallet(@AwesomeParam(required = false) Long userId) {
        if (userId == null && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_WALLET)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        if (userId != null && authSession.getAuthId() != userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return walletService.getWallet(userId);
    }

}
