package com.ylli.api.auth;

import com.ylli.api.auth.service.LoginService;
import com.ylli.api.base.exception.AwesomeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 超级管理员的登录接口
 * !!! 注意 !!!
 * 此接口必须严格控制访问
 * Created by ylli on 2018/11/21.
 */
@RestController
@RequestMapping("/login/super")
public class SuperAuthController {

    @Autowired
    LoginService loginService;

    @PostMapping
    public Object login() throws AwesomeException {
        return loginService.login(Config.SUPER_MAN_ID);
    }
}
