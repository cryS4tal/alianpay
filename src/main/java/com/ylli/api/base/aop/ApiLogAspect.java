package com.ylli.api.base.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Created by ylli on 2017/2/10.
 */
@Aspect
@Component
public class ApiLogAspect {

    private static Logger LOGGER = LoggerFactory.getLogger(ApiLogAspect.class);

    @Pointcut("@within(org.springframework.web.bind.annotation.RequestMapping)")
    @Order(OrderDef.ORDER_API_LOG)
    public void apiLog(){}

    @Before("apiLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        StringBuilder sb = new StringBuilder();
        sb.append("\n==========REQUEST===========");
        sb.append("\nURL : ").append(request.getRequestURL().toString());
        sb.append("\nHTTP_METHOD : ").append(request.getMethod());
        sb.append("\nIP : ").append(request.getRemoteAddr());
        sb.append("\nCLASS_METHOD : ")
                .append(joinPoint.getSignature().getDeclaringTypeName())
                .append(".")
                .append(joinPoint.getSignature().getName());
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            for (Object arg: joinPoint.getArgs()) {
                sb.append("\n").append(writer.writeValueAsString(arg));
            }
        } catch (Exception ignored) {
            //do noting
            ;
        }
        sb.append("\n============================");
        LOGGER.info(sb.toString());
    }

    @AfterReturning(returning = "ret", pointcut = "apiLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==========RESPONSE===========");
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = writer.writeValueAsString(ret);
        sb.append("\n").append(json);
        sb.append("\n=============================");
        LOGGER.info(sb.toString());
    }

    @AfterThrowing(throwing = "ex", pointcut = "apiLog()")
    public void doAfterThrowing(Exception ex) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==========RESPONSE===========");
        sb.append("\nEXCEPTION: ").append(ex);
        sb.append("\n=============================");
        LOGGER.info(sb.toString());
    }

    @After("apiLog()")
    public void doAfter() throws Throwable {
    }
}
