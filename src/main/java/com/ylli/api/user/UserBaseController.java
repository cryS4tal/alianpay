package com.ylli.api.user;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.user.model.UserBase;
import com.ylli.api.user.service.UserBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * userBase信息用于平台代付商户
 */
@Auth
@RestController
@RequestMapping("/user/base")
public class UserBaseController {

    @Autowired
    UserBaseService userBaseService;

    @Autowired
    AuthSession authSession;

    @PostMapping
    public void register(@RequestBody UserBase userBase) {
        if (userBase.userType == null || (userBase.userType != UserBase.COMPANY && userBase.userType != UserBase.PERSON)) {
            throw new AwesomeException(Config.ERROR_USER_TYPE);
        }
        if (userBase.userType == UserBase.COMPANY) {
            ServiceUtil.checkNotEmptyIgnore(userBase, true);
        } else {
            ServiceUtil.checkNotEmptyIgnore(userBase, true, "companyName", "address", "businessLicense", "legalPerson", "legalPhone");
        }
        if (authSession.getAuthId() != userBase.userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        userBaseService.register(userBase);
    }
}
