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


    /**
     * 获取用户钱包.
     * 用户获取自己
     * 管理员获取所有
     *
     * @return
     */
    @GetMapping
    public Object getWallet(@AwesomeParam Long mchId) {
        if (mchId != authSession.getAuthId() && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_WALLET)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return walletService.getOwnWallet(mchId);
    }

    static class Conversion {
        public Long mchId;
        public Integer money;
    }

    @PostMapping("/conversion")
    public Object conversion(@RequestBody Conversion conversion) {
        if (authSession.getAuthId() != conversion.mchId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return walletService.conversion(conversion.mchId, conversion.money);
    }

    static class Recharge {
        public Long mchId;
        public Integer money;
    }

    @PostMapping("/recharge")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_WALLET))
    public Object recharge(@RequestBody Recharge recharge) {
        return walletService.recharge(recharge.mchId, recharge.money);
    }

}
