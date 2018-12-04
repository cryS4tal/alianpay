package com.ylli.api.auth;

import com.google.common.base.Strings;
import com.ylli.api.auth.mapper.AccountPasswordMapper;
import com.ylli.api.auth.model.Account;
import com.ylli.api.auth.model.AccountPassword;
import com.ylli.api.auth.model.PhoneLogin;
import com.ylli.api.auth.service.LoginService;
import com.ylli.api.auth.service.PhoneAuthService;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login/phone")
public class PhoneAuthController {

    @Autowired
    PhoneAuthService phoneAuthService;

    @Autowired
    LoginService loginService;

    @Autowired
    AccountPasswordMapper accountPasswordMapper;

    @Autowired
    WalletService walletService;

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

            //插入accountPassword
            AccountPassword password = new AccountPassword();
            password.id = account.id;
            password.password = BCrypt.hashpw(request.password, BCrypt.gensalt());
            accountPasswordMapper.insertSelective(password);

            walletService.create(account.id);

        } else {
            AccountPassword password = accountPasswordMapper.selectByPrimaryKey(account.id);

            if (Strings.isNullOrEmpty(request.password) || !BCrypt.checkpw(request.password, password.password)) {
                throw new AwesomeException(Config.ERROR_VERIFY);
            }
        }
        return loginService.login(account.id);
    }

}
