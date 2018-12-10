package com.ylli.api.user;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.CheckPhone;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.user.model.Audit;
import com.ylli.api.user.model.UserBase;
import com.ylli.api.user.service.UserBaseService;
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
public class UserBaseController {

    @Autowired
    UserBaseService userBaseService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    @PostMapping
    public void register(@RequestBody UserBase userBase) {
        ServiceUtil.checkNotEmptyIgnore(userBase, true, "nickName", "linkName", "linkPhone", "businessLicense");
        if (userBase.mchId != authSession.getAuthId()) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        if (!CheckPhone.isPhoneOrTel(userBase.legalPhone) || (userBase.linkPhone != null && !CheckPhone.isPhoneOrTel(userBase.linkPhone))) {
            throw new AwesomeException(Config.ERROR_ILLEGAL_PHONE);
        }
        userBaseService.register(userBase);
    }

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
        return userBaseService.getBase(mchId, state, mchName, name, phone, businessLicense, offset, limit);
    }

    @PostMapping("/audit")
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BASE))
    public Object audit(@RequestBody Audit audit) {
        ServiceUtil.checkNotEmpty(audit);
        return userBaseService.audit(audit.mchId, audit.state);
    }

    @PutMapping
    @Auth(@Permission(Config.SysPermission.MANAGE_USER_BASE))
    public Object update(@RequestBody UserBase userBase) {
        if (!CheckPhone.isPhoneOrTel(userBase.legalPhone) || (userBase.linkPhone != null && !CheckPhone.isPhoneOrTel(userBase.linkPhone))) {
            throw new AwesomeException(Config.ERROR_ILLEGAL_PHONE);
        }
        return userBaseService.update(userBase);
    }
}
