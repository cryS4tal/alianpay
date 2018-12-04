package com.ylli.api.user;

import com.google.common.base.Strings;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.user.service.UserManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Auth(@Permission(Config.SysPermission.MANAGE_USER_ACCOUNT))
public class UserManageController {

    @Autowired
    UserManageService userManageService;

    @GetMapping("/user/list")
    public Object getAccountList(@AwesomeParam(required = false) String phone,
                                 @AwesomeParam(required = false) String mchId,
                                 @AwesomeParam(defaultValue = "0") int offset,
                                 @AwesomeParam(defaultValue = "10") int limit) {

        return userManageService.getAccountList(Strings.emptyToNull(phone), Strings.emptyToNull(mchId), offset, limit);
    }

    @GetMapping("/user")
    public Object getAccountDetail(@AwesomeParam Long mchId) {
        return userManageService.getAccountDetail(mchId);
    }
}
