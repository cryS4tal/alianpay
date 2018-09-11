package com.ylli.api.base;

import com.ylli.api.base.exception.ErrorCode;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by RexQian on 2017/2/21.
 */
public class Config {

    /**
     * 模块编号
     */
    public static final int MODEL_CODE = 1;

    public static final ErrorCode ERROR_NO_AUTH
            = new ErrorCode(HttpServletResponse.SC_UNAUTHORIZED, MODEL_CODE,
            1, "未登录");

    public static final ErrorCode ERROR_ACCESS_DENY
            = new ErrorCode(HttpServletResponse.SC_FORBIDDEN, MODEL_CODE,
            2, "未授权");

    public static final ErrorCode ERROR_INTERNAL
            = new ErrorCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MODEL_CODE,
            3, "未预料的错误");

    public static final ErrorCode ERROR_BAD_PARAM
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            4, "非法的请求参数");

    public static final ErrorCode ERROR_DEBUG_ONLY
            = new ErrorCode(HttpServletResponse.SC_BAD_REQUEST, MODEL_CODE,
            5, "仅调试模式可以");
    /**
     * 超级管理员Id
     */
    public static final long SUPER_MAN_ID = 1;
}
