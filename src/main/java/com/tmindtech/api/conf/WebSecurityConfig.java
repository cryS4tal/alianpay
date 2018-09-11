package com.tmindtech.api.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmindtech.api.model.base.ErrorMessage;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

/**
 * Created by RexQian on 2017/2/14.
 */
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${csrf_enabled}")
    private boolean isCsrfEnabled;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /**
         * 配置XSRF验证
         */
        if (!isCsrfEnabled) {
            http.csrf().disable();
        } else {
            /**
             * 排除登录相关接口
             */
            http.csrf()
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringAntMatchers("/login");

            http.exceptionHandling().accessDeniedHandler(
                (request, response, accessDeniedException) -> {
                    if (accessDeniedException instanceof MissingCsrfTokenException
                            || accessDeniedException instanceof InvalidCsrfTokenException) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        ErrorMessage message = new ErrorMessage();
                        message.code = HttpServletResponse.SC_FORBIDDEN;
                        message.message = "XSRF验证失败";
                        message.debugMessage = "在请求中附加_csrf参数或X-XSRF-TOKEN头";
                        ObjectMapper mapper = new ObjectMapper();
                        response.getWriter().print(mapper.writeValueAsString(message));
                    }
                });
        }
    }
}
