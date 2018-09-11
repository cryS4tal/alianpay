package com.ylli.api.base.auth;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.base.Config;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Session认证帮助类.
 * 仅支持单点登录。其它session登录会引起登录失效。
 * Created by RexQian on 2017/2/12.
 */
@Component
public class AuthSession {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Value("${single-login}")
    boolean singleLogin;

    //auth session key
    private static final String AUTH_KEY = "auth";
    //redis key: "user:{id}:auth"
    private static final String REDIS_AUTH_KEY = "user:%s:auth";

    /**
     * @return Session是否已经认证.
     */
    public boolean isAuth() {
        long uid = getAuthId();
        if (uid == 0) {
            return false;
        }
        if (!singleLogin) {
            return true;
        }
        String sessionId = redisTemplate.opsForValue().get(String.format(REDIS_AUTH_KEY, uid));
        return getSession().getId().equals(sessionId);
    }

    /**
     * 设置指定用户为当前登录用户
     *
     * @param uid 用户Id
     */
    public void setAuth(long uid) {
        removeAuth();

        HttpSession session = getSession();
        session.setAttribute(AUTH_KEY, uid);
        redisTemplate.opsForValue().set(String.format(REDIS_AUTH_KEY, uid),
                session.getId());
    }

    /**
     * @return 返回认证的用户id，当未认证或认证失效时，返回0.
     */
    public long getAuthId() {
        try {
            Object result = getSession().getAttribute(AUTH_KEY);
            if (result == null) {
                return 0;
            }
            return (long) result;
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除当前Session的登录状态
     */
    public void removeAuth() {
        long uid = getAuthId();
        if (uid == 0) {
            return;
        }
        getSession().removeAttribute(AUTH_KEY);
        String key = String.format(REDIS_AUTH_KEY, uid);
        String sessionId = redisTemplate.opsForValue().get(key);
        if (getSession().getId().equals(sessionId)) {
            redisTemplate.delete(key);
        }
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes.getRequest().getSession();
    }

    public void authDeny() {
        throw new AwesomeException(Config.ERROR_NO_AUTH);
    }
}
