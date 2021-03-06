package com.ylli.api.base.aop;

import com.ylli.api.base.Config;
import com.ylli.api.base.exception.AwesomeException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Created by ylli on 2017/3/15.
 */
@Aspect
@Component
public class DebugAspect {

    @Value("${debug}")
    private boolean isDebug;

    @Pointcut("@annotation(com.ylli.api.base.annotation.Debug)"
            + " || @within(com.ylli.api.base.annotation.Debug)")
    @Order(OrderDef.ORDER_DEBUG)
    public void guard() {
    }

    @Before("guard()")
    private void doGuard(JoinPoint joinPoint)
            throws AwesomeException {

        if (!isDebug) {
            throw new AwesomeException(Config.ERROR_DEBUG_ONLY);
        }
    }
}
