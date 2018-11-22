package com.ylli.api.user;

import com.ylli.api.auth.service.PermissionService;
import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.AwesomeParam;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.user.service.UserAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/app")
@Auth
public class UserAppController {

    @Autowired
    UserAppService userAppService;

    @Autowired
    AuthSession authSession;

    @Autowired
    PermissionService permissionService;

    static class App {
        public Long userId;
        public String appName;
    }

    @PostMapping
    public void createApp(@RequestBody App app) {
        if (authSession.getAuthId() != app.userId) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        userAppService.createApp(app.userId, app.appName);
    }

    @GetMapping
    public Object getApps(@AwesomeParam(required = false) Long userId,
                          @AwesomeParam(defaultValue = "0") int offset,
                          @AwesomeParam(defaultValue = "10") int limit) {

        if (authSession.getAuthId() != (userId == null ? 0 : userId) && !permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_APP)) {
            throw new AwesomeException(Config.ERROR_PERMISSION_DENY);
        }
        return userAppService.getApps(userId, offset, limit);
    }

    static class Switch {
        public Long userId;
        public String appId;
        //true = 启用，false = 弃用
        public Boolean status;
    }

    @PutMapping
    public void appSwitch(@RequestBody Switch s) {
        userAppService.appSwitch(s.userId, s.appId, s.status, permissionService.hasSysPermission(Config.SysPermission.MANAGE_USER_APP));
    }

    @DeleteMapping("/{id}")
    public void removeApp(@PathVariable long id) {
        userAppService.removeApp(id);
    }

}
