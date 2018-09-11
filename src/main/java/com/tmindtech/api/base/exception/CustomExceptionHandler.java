package com.tmindtech.api.base.exception;

import com.tmindtech.api.base.Config;
import com.tmindtech.api.model.base.ErrorMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


/**
 * 全局捕获自定义的异常，返回格式化后的标准输出
 * 异常捕获优先级设定高于{@link GlobalExceptionHandler}
 *
 * @see AwesomeException
 * @see GlobalExceptionHandler
 *
 * Created by RexQian on 2017/2/9.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomExceptionHandler {

    @Value("${debug}")
    private boolean isDebug;

    @ExceptionHandler(AwesomeException.class)
    @ResponseBody
    public ErrorMessage handleError(HttpServletRequest req,
                                    HttpServletResponse rsp, AwesomeException ex) {
        rsp.setStatus(ex.statusCode);
        return toErrorMessage(ex);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class,
            ServletRequestBindingException.class, MethodArgumentTypeMismatchException.class})
    @ResponseBody
    public ErrorMessage handleError(HttpServletRequest req,
                                    HttpServletResponse rsp,
                                    Exception ex) {
        rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.code = Config.ERROR_BAD_PARAM.getCode();
        errorMessage.message = Config.ERROR_BAD_PARAM.getMessage();

        //should not output exception in Production Env
        if (isDebug) {
            errorMessage.debugMessage = ExceptionHelper.getStackTrace(ex);
        }

        return errorMessage;
    }

    private ErrorMessage toErrorMessage(AwesomeException ex) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.code = ex.code;
        errorMessage.message = ex.getMessage();

        //should not output exception in Production Env
        if (isDebug) {
            errorMessage.debugMessage = ExceptionHelper.getStackTrace(ex);
        }
        return errorMessage;
    }
}
