package com.ylli.api.base.aop;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.annotation.Permission;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.Config;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by ylli on 2017/2/10.
 */
@Aspect
@Component
public class AuthAspect {

    private static Logger LOGGER = Logger.getLogger(AuthAspect.class.getName());

    @Autowired
    AuthSession authSession;

    private PermissionCheckCallback permissionCheckCallback;

    @Pointcut("@annotation(com.ylli.api.base.annotation.Auth)"
            + " || @within(com.ylli.api.base.annotation.Auth)")
    @Order(OrderDef.ORDER_AUTH)
    public void guard() {
    }

    private boolean hasPermission(Permission[] permissions) throws AwesomeException {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        boolean result = false;
        if (permissionCheckCallback != null) {
            result = permissionCheckCallback.check(permissions);
        }
        return result;
    }

    private Auth getAuth(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        //先获取方法上的注解
        Auth auth = method.getAnnotation(Auth.class);
        if (auth == null) {
            //得到类上的访问注解
            auth = joinPoint.getTarget().getClass().getAnnotation(Auth.class);
        }
        return auth;
    }

    @Before("guard()")
    private void doGuard(JoinPoint joinPoint)
            throws AwesomeException {
        Auth auth = getAuth(joinPoint);
        if (!authSession.isAuth()) {
            authSession.authDeny();
        }

        // 超级管理员豁免所有权限
        if (authSession.getAuthId() == Config.SUPER_MAN_ID) {
            return;
        }

        if (!hasPermission(auth.value())) {
            throw new AwesomeException(Config.ERROR_ACCESS_DENY);
        }
    }

    public interface PermissionCheckCallback {
        boolean check(Permission[] permissions) throws AwesomeException;
    }

    public void setPermissionCheckCallback(PermissionCheckCallback callback) {
        this.permissionCheckCallback = callback;
    }
}
