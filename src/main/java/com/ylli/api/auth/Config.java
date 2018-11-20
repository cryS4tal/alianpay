package com.ylli.api.auth;

import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 4;
    /**
     * 错误定义
     */
    public static final ErrorCode ERROR_EXAMPLE_NOT_FOUND
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            1, "示例不存在");

    public static final ErrorCode ERROR_INVALID_PHONE
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            2, "无效的手机号码");

    public static final ErrorCode ERROR_USER_DISABLE
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            3, "用户已被禁用");

    public static final ErrorCode ERROR_VERIFY
            = new ErrorCode(HttpServletResponse.SC_NOT_FOUND, MODEL_CODE,
            4, "账号密码校验错误");
}
