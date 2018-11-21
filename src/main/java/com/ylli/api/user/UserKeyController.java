package com.ylli.api.user;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.user.service.UserKeyService;
import org.springframework.beans.factory.annotation.Autowired;
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

    static class Key {
        public Long userId;
        public String secretKey;
    }

    @PostMapping
    public void saveKey(@RequestBody Key key) {
        if (authSession.getAuthId() != key.userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        userKeyService.saveKey(key.userId, key.secretKey);
    }
}
