package com.ylli.api.base.exception;

import com.ylli.api.model.base.ErrorMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * 全局默认异常捕获
 * 捕获优先级最低
 *
 * Created by ylli on 2017/2/9.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @Value("${debug}")
    private boolean isDebug;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleError(HttpServletRequest req, Exception ex) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        errorMessage.message = "服务器错误，请联系管理员 >.<";

        //should not output exception in Production Env
        if (isDebug) {
            errorMessage.debugMessage = ExceptionHelper.getStackTrace(ex);
        }
        return errorMessage;
    }

}
