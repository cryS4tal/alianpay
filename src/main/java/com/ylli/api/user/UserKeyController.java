package com.ylli.api.user;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.util.ServiceUtil;
import com.ylli.api.user.model.Key;
import com.ylli.api.user.model.UserKeyRes;
import com.ylli.api.user.service.UserKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/secret")
@Auth
public class UserKeyController {

    @Autowired
    UserKeyService userKeyService;

    @Autowired
    AuthSession authSession;

    @PostMapping
    public UserKeyRes saveKey(@RequestBody Key key) {
        ServiceUtil.checkNotEmpty(key);
        if (authSession.getAuthId() != key.userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return userKeyService.saveKey(key.userId, key.secretKey);
    }

    @PostMapping("/random")
    public UserKeyRes randomKey() {
        return userKeyService.randomKey();
    }

    @GetMapping
    public UserKeyRes getKey(@AwesomeParam Long userId) {
        if (authSession.getAuthId() != userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return userKeyService.getKey(userId);
    }

}
