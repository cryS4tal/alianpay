package com.ylli.api.wallet;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.wallet.model.MchRecharge;
import com.ylli.api.wallet.service.WalletLogService;
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

    @Autowired
    WalletLogService walletLogService;

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

    static class Recharge {
        public Long mchId;
        public Integer money;
        public String password;
    }

    /**
     * 代付充值 - 管理员.
     *
     * @param recharge
     * @return
     */
    @PostMapping("/recharge")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_WALLET))
    public Object recharge(@RequestBody Recharge recharge) {
        return walletService.recharge(authSession.getAuthId(), recharge.mchId, recharge.money, recharge.password);
    }

    /**
     * 代付充值 - 商户.
     */
    @PostMapping("/recharge/mch")
    @Auth
    public Object rechargeMch(@RequestBody MchRecharge mchRecharge) throws Exception {
        ServiceUtil.checkNotEmpty(mchRecharge);
        if (authSession.getAuthId() != mchRecharge.mchId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return walletService.rechargeMch(mchRecharge.mchId, mchRecharge.money, mchRecharge.accountName, mchRecharge.accountNo, mchRecharge.recevieBank);
    }

    /**
     * 代付充值 - 商户.
     * 单笔订单查询
     */
    @Auth
    @GetMapping("/recharge/mch")
    public Object rechargeQuery(@AwesomeParam String id) throws Exception {
        return walletService.rechargeQuery(authSession.getAuthId(), id);
    }

    @GetMapping("/recharge/log")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_WALLET))
    public Object getLogs(@AwesomeParam Long mchId,
                          @AwesomeParam(defaultValue = "0") int offset,
                          @AwesomeParam(defaultValue = "20") int limit) {
        return walletLogService.getLogs(mchId, offset, limit);
    }
}
