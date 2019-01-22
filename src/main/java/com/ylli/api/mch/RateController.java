package com.ylli.api.mch;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.mch.model.Apps;
import com.ylli.api.mch.model.SysApp;
import com.ylli.api.mch.service.RateService;
import com.ylli.api.pay.service.BankPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Auth
public class RateController {

    @Autowired
    RateService rateService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    @Autowired
    BankPayService bankPayService;

    @PostMapping("/sys/app")
    @Auth(@Permission(Config.SysPermission.MANAGE_APP))
    public Object createApp(@RequestBody SysApp app) {
        ServiceUtil.checkNotEmptyIgnore(app, true, "status");
        return rateService.createApp(app.rate, app.appName);
    }

    @GetMapping("/sys/app")
    @Auth(@Permission(Config.SysPermission.MANAGE_APP))
    public Object getSysApp(@AwesomeParam(required = false) String appName,
                            @AwesomeParam(required = false) Boolean status,
                            @AwesomeParam(defaultValue = "0") int offset,
                            @AwesomeParam(defaultValue = "10") int limit) {
        return rateService.getSysApp(appName, status, offset, limit);
    }

    @PutMapping("/sys/app")
    @Auth(@Permission(Config.SysPermission.MANAGE_APP))
    public Object updateApp(@RequestBody SysApp app) {
        return rateService.updateApp(app);
    }


    @PostMapping("/mch/app")
    @Auth(@Permission(Config.SysPermission.MANAGE_APP))
    public Object setUserRate(@RequestBody Apps apps) {
        return rateService.setUserRate(apps);
    }

    @DeleteMapping("/mch/app")
    @Auth(@Permission(Config.SysPermission.MANAGE_APP))
    public void removeApp(@AwesomeParam Long appId,
                          @AwesomeParam Long mchId) {
        rateService.removeApp(appId, mchId);
    }

    @GetMapping("/mch/app")
    public Object getMchApp(@AwesomeParam Long mchId) {
        if (mchId != authSession.getAuthId() && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_APP)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return rateService.getMchApp(mchId);
    }

    static class BankPayRate {
        public Long mchId;
        public Integer rate;
    }

    /**
     * 设置商户代付费率
     */
    @PostMapping("/bankpay/mch/rate")
    public void bankPayRate(@RequestBody BankPayRate rate) {
        bankPayService.bankPayRate(rate.mchId, rate.rate);
    }

    @GetMapping("/bankpay/mch/rate")
    public Object getBankPayRate(@AwesomeParam Long mchId) {
        return bankPayService.getBankPayRate(mchId);
    }
}
