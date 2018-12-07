package com.ylli.api.auth;

import com.ylli.api.auth.model.Forget;
import com.ylli.api.auth.service.PasswordService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用于用户修改密码 / 交易密码.
 * 待接入短信验证。
 */
@RestController
@RequestMapping("/pwd")
@Auth
public class PwdController {

    @Autowired
    PasswordService pwdService;

    @Autowired
    AuthSession authSession;

    @PostMapping("/forget")
    public void forgetPwd(@RequestBody Forget forget) {
        ServiceUtil.checkNotEmpty(forget);
        if (authSession.getAuthId() != forget.userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        pwdService.forgetPwd(forget.userId, forget.oldPwd, forget.newPwd);
    }

    @PostMapping("/reset")
    public void resetPwd() {

    }
}
