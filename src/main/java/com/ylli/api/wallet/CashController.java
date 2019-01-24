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

    /**
     * 商户发起提现请求
     *
     * @param req
     */
    @PostMapping
    public void cash(@RequestBody CashReq req) throws Exception {
        ServiceUtil.checkNotEmptyIgnore(req, true, "identityCard", "reservedPhone", "subBank");
        if (authSession.getAuthId() != req.mchId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        cashService.cash(req);
    }

    /**
     * 获得商户最近10次提现成功银行卡信息列表
     */
    @GetMapping
    public Object bankList() {
        return cashService.bankList(authSession.getAuthId());
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
        cashService.manualCash(suc.cashLogId, suc.success, authSession.getAuthId());
    }

    /**
     * 系统代付
     */
    /*static class Sys {
        //系统代付通道
        public Long bankPayId;
        public Long cashLogId;
    }

    @PostMapping("/sys")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_CASH))
    public void sysCash(@RequestBody Sys sys) throws Exception {
        //cashService.sysCash(sys.bankPayId, sys.cashLogId);
    }*/
}
