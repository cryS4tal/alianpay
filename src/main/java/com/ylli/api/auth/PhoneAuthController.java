package com.ylli.api.auth;

import com.google.common.base.Strings;
import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.PhoneLogin;
import com.ylli.api.auth.service.LoginService;
import com.ylli.api.auth.service.PasswordService;
import com.ylli.api.auth.service.PhoneAuthService;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.phone.service.SmsService;
import com.ylli.api.user.service.UserBaseService;
import com.ylli.api.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login/phone")
public class PhoneAuthController {

    @Autowired
    SmsService smsService;

    @Autowired
    PhoneAuthService phoneAuthService;

    @Autowired
    LoginService loginService;

    @Autowired
    PasswordService passwordService;

    @Autowired
    WalletService walletService;

    @Autowired
    UserBaseService userBaseService;

    /**
     * 未接入短信服务.暂时使用密码校验（待移除）.
     *
     * @param request
     * @return
     */
    @PostMapping
    public Object login(@RequestBody PhoneLogin request) {

        if (Strings.isNullOrEmpty(request.phone)) {
            throw new AwesomeException(Config.ERROR_PHONE_NOT_EMPTY);
        }
        if (Strings.isNullOrEmpty(request.password)) {
            throw new AwesomeException(Config.ERROR_PASSWORD_NOT_EMPTY);
        }

        //todo 接入短信认证
        //smsService.checkVerifyCode(request.phone, request.code);

        Account account = phoneAuthService.getByPhone(request.phone);
        if (account == null) {
            account = phoneAuthService.create(request.phone);

            passwordService.init(account.id, request.password);
            walletService.init(account.id);
            userBaseService.init(account.id, request.phone);
        } else {
            //密码校验..
            passwordService.checkpw(account.id, request.password);
        }
        return loginService.login(account.id);
    }

}
