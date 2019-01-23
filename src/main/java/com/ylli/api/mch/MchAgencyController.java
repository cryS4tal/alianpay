package com.ylli.api.mch;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.mch.model.MchAgency;
import com.ylli.api.mch.service.MchAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/mch/agency")
public class MchAgencyController {

    @Autowired
    MchAgencyService mchAgencyService;

    @Autowired
    PermissionService permissionService;

    @Autowired
    AuthSession authSession;

    @PostMapping
    @Auth(@Permission(Config.SysPermission.MANAGE_AGENCY))
    public void addSub(@RequestBody MchAgency mchAgency) {
        ServiceUtil.checkNotEmptyIgnore(mchAgency, true, "alipayRate", "wxRate", "bankRate", "mchName",
                "subName", "supAlipayRate", "subAlipayRate", "supWxRate", "subWxRate", "supRate", "subRate");
        mchAgencyService.addSub(mchAgency.mchId, mchAgency.subId, mchAgency.type);
    }

    @Auth
    @GetMapping("/available")
    public Object isAgency(@AwesomeParam Integer type) {
        return mchAgencyService.isAgency(authSession.getAuthId(), type);
    }


    @DeleteMapping("/{id}")
    @Auth(@Permission(Config.SysPermission.MANAGE_AGENCY))
    public void delete(@PathVariable Long id) {
        mchAgencyService.delete(id);
    }

    @GetMapping
    @Auth(@Permission(Config.SysPermission.MANAGE_AGENCY))
    public Object agencyList(@AwesomeParam Integer type,
                             @AwesomeParam(required = false) Long mchId,
                             @AwesomeParam(required = false) Long subId,
                             @AwesomeParam(defaultValue = "0") int offset,
                             @AwesomeParam(defaultValue = "10") int limit) {
        do {
            if (mchId != null && authSession.getAuthId() == mchId) {
                break;
            }
            if (permissionService.hasSysPermission(Config.SysPermission.MANAGE_AGENCY)) {
                break;
            }
            permissionService.permissionDeny();
        } while (false);

        return mchAgencyService.agencyList(type, mchId, subId, offset, limit);
    }
}
