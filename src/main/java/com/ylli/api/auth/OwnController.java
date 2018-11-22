package com.ylli.api.auth;

import com.ylli.api.auth.service.LoginService;
import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.auth.AuthSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by RexQian on 2017/2/21.
 */
@RestController
@RequestMapping("/own")
@Auth
public class OwnController {

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    @Autowired
    LoginService loginService;

    @GetMapping
    public Object getOwnInfo() {
        return loginService.getOwnInfo();
    }

    @DeleteMapping
    public void logout() {
        authSession.removeAuth();
    }

}
