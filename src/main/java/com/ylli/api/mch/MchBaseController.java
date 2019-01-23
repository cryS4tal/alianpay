package com.ylli.api.mch;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.CheckPhone;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.mch.model.Audit;
import com.ylli.api.mch.model.MchBase;
import com.ylli.api.mch.service.MchBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Auth
@RestController
@RequestMapping("/user/base")
public class MchBaseController {

    @Autowired
    MchBaseService mchBaseService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    /**
     * 基础信息注册
     *
     * @param mchBase
     */
    @PostMapping
    public void register(@RequestBody MchBase mchBase) {
        ServiceUtil.checkNotEmptyIgnore(mchBase, true, "nickName", "linkName", "linkPhone", "businessLicense", "state");
        if (mchBase.mchId != authSession.getAuthId()) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        if (!CheckPhone.isSimplePhone(mchBase.legalPhone) || (mchBase.linkPhone != null && !CheckPhone.isSimplePhone(mchBase.linkPhone))) {
            throw new AwesomeException(Config.ERROR_ILLEGAL_PHONE);
        }
        mchBaseService.register(mchBase);
    }

    /**
     * 现在只用于商户基础信息获取。query字段 多余
     */
    @GetMapping
    public Object getBase(@AwesomeParam(required = false) Long mchId,
                          @AwesomeParam(required = false) Integer state,
                          @AwesomeParam(required = false) String mchName,
                          @AwesomeParam(required = false) String name,
                          @AwesomeParam(required = false) String phone,
                          @AwesomeParam(required = false) String businessLicense,
                          @AwesomeParam(defaultValue = "0") int offset,
                          @AwesomeParam(defaultValue = "10") int limit) {
        if (mchId == null && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_BASE)) {
            permissionService.permissionDeny();
        }
        return mchBaseService.getBase(mchId, state, mchName, name, phone, businessLicense, offset, limit);
    }


    /**
     * 管理员基础信息审核
     */
    @PostMapping("/audit")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BASE))
    public Object audit(@RequestBody Audit audit) {
        ServiceUtil.checkNotEmpty(audit);
        return mchBaseService.audit(audit.mchId, audit.state);
    }

    /**
     * 管理员基础信息更新
     */
    @PutMapping
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BASE))
    public Object update(@RequestBody MchBase mchBase) {
        if (!CheckPhone.isSimplePhone(mchBase.legalPhone) || (mchBase.linkPhone != null && !CheckPhone.isSimplePhone(mchBase.linkPhone))) {
            throw new AwesomeException(Config.ERROR_ILLEGAL_PHONE);
        }
        return mchBaseService.update(mchBase);
    }
}
